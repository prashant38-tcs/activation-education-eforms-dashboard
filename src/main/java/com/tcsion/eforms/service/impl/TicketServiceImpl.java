package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.dto.request.*;
import com.tcsion.eforms.entity.*;
import com.tcsion.eforms.exception.BusinessValidationException;
import com.tcsion.eforms.exception.DuplicateResourceException;
import com.tcsion.eforms.exception.OptimisticLockConflictException;
import com.tcsion.eforms.exception.ResourceNotFoundException;
import com.tcsion.eforms.repository.*;
import com.tcsion.eforms.security.CustomUserDetails;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.security.TicketAccessGuard;
import com.tcsion.eforms.service.*;
import com.tcsion.eforms.workflow.TicketWorkflowService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerMasterRepository customerMasterRepository;
    private final TicketTypeMasterRepository ticketTypeMasterRepository;
    private final PriorityMasterRepository priorityMasterRepository;
    private final SeverityMasterRepository severityMasterRepository;
    private final StatusMasterRepository statusMasterRepository;
    private final UserRepository userRepository;
    private final TeamMasterRepository teamMasterRepository;
    private final ActivityTypeMasterRepository activityTypeMasterRepository;
    private final TicketAssignmentRepository ticketAssignmentRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final FrontendChangeRepository frontendChangeRepository;
    private final BackendChangeRepository backendChangeRepository;
    private final DatabaseChangeRepository databaseChangeRepository;

    private final TicketAccessGuard ticketAccessGuard;
    private final TicketWorkflowService ticketWorkflowService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final WsrService wsrService;
    private final AgingService agingService;
    private final SlaRiskService slaRiskService;

    @Override
    @Transactional
    public Ticket createTicket(TicketCreateRequest request) {
        if (ticketRepository.existsByTicketNumberIgnoreCase(request.getTicketNumber())) {
            throw new DuplicateResourceException("Ticket Number '" + request.getTicketNumber() + "' already exists.");
        }
        CustomerMaster customer = customerMasterRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        TicketTypeMaster type = ticketTypeMasterRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found."));
        PriorityMaster priority = priorityMasterRepository.findById(request.getPriorityId())
                .orElseThrow(() -> new ResourceNotFoundException("Priority not found."));
        SeverityMaster severity = severityMasterRepository.findById(request.getSeverityId())
                .orElseThrow(() -> new ResourceNotFoundException("Severity not found."));
        StatusMaster newStatus = statusMasterRepository.findByStatusCode(StatusMaster.NEW)
                .orElseThrow(() -> new ResourceNotFoundException("Default status configuration missing."));

        User assignedUser = null;
        if (request.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedUserId())
                    .filter(User::isActive)
                    .orElseThrow(() -> new BusinessValidationException("Assigned developer must be an active user."));
        }
        TeamMaster sourceTeam = request.getSourceTeamId() != null
                ? teamMasterRepository.findById(request.getSourceTeamId()).orElse(null) : null;
        TeamMaster dependencyTeam = request.getDependencyTeamId() != null
                ? teamMasterRepository.findById(request.getDependencyTeamId()).orElse(null) : null;

        if (request.getEstimatedProductionDate() != null && request.getExpectedClosureDate() != null
                && request.getEstimatedProductionDate().isBefore(request.getExpectedClosureDate())) {
            throw new BusinessValidationException("Estimated Production Date cannot precede the Expected Closure Date.");
        }

        CustomUserDetails currentUser = SecurityUtils.currentUser()
                .orElseThrow(() -> new BusinessValidationException("Authentication is required."));

        Ticket ticket = Ticket.builder()
                .ticketNumber(request.getTicketNumber().trim())
                .crmId(StringUtils.trimToNull(request.getCrmId()))
                .customer(customer)
                .ticketTitle(request.getTicketTitle().trim())
                .ticketDescription(request.getTicketDescription())
                .shortPlannedMilestone(request.getShortPlannedMilestone())
                .ticketType(type).priority(priority).severity(severity)
                .assignedUser(assignedUser)
                .assignedBy(assignedUser != null ? userRepository.findById(currentUser.getUserId()).orElse(null) : null)
                .assignmentDate(assignedUser != null ? LocalDateTime.now() : null)
                .sourceTeam(sourceTeam).dependencyTeam(dependencyTeam)
                .currentStatus(newStatus)
                .expectedClosureDate(request.getExpectedClosureDate())
                .estimatedProductionDate(request.getEstimatedProductionDate())
                .build();
        ticket = ticketRepository.save(ticket);

        if (assignedUser != null) {
            recordAssignment(ticket, null, assignedUser, null, TicketAssignment.INITIAL, "Initial ticket creation");
            notificationService.notify(assignedUser, Notification.NEW_TICKET_ASSIGNED,
                    "New ticket assigned: " + ticket.getTicketNumber(),
                    "Ticket " + ticket.getTicketNumber() + " (" + ticket.getTicketTitle() + ") has been assigned to you.",
                    ticket);
        }
        agingService.recalculateAging(ticket);
        slaRiskService.recalculateRisk(ticket);
        auditService.log("TICKET_CREATED", "TICKET", ticket.getId(), ticket.getTicketNumber(), null, "Created with status NEW");
        return ticket;
    }

    @Override
    @Transactional
    public Ticket updateTicket(Long ticketId, TicketUpdateRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        assertAdministrativeAccess();
        checkOptimisticLock(ticket, request.getExpectedVersion());

        if (request.getEstimatedProductionDate() != null && ticket.getAssignmentDate() != null
                && request.getEstimatedProductionDate().isBefore(ticket.getAssignmentDate().toLocalDate())) {
            throw new BusinessValidationException("Estimated Production Date cannot precede the Assignment Date.");
        }

        TicketTypeMaster type = ticketTypeMasterRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found."));
        PriorityMaster priority = priorityMasterRepository.findById(request.getPriorityId())
                .orElseThrow(() -> new ResourceNotFoundException("Priority not found."));
        SeverityMaster severity = severityMasterRepository.findById(request.getSeverityId())
                .orElseThrow(() -> new ResourceNotFoundException("Severity not found."));
        TeamMaster dependencyTeam = request.getDependencyTeamId() != null
                ? teamMasterRepository.findById(request.getDependencyTeamId()).orElse(null) : null;

        String oldValue = "title=" + ticket.getTicketTitle() + ";priority=" + ticket.getPriority().getPriorityCode();

        ticket.setTicketTitle(request.getTicketTitle().trim());
        ticket.setTicketDescription(request.getTicketDescription());
        ticket.setShortPlannedMilestone(request.getShortPlannedMilestone());
        ticket.setTicketType(type);
        ticket.setPriority(priority);
        ticket.setSeverity(severity);
        ticket.setDependencyTeam(dependencyTeam);
        ticket.setExpectedClosureDate(request.getExpectedClosureDate());
        ticket.setEstimatedProductionDate(request.getEstimatedProductionDate());
        ticket.setLastUpdatedDate(LocalDateTime.now());

        try {
            ticket = ticketRepository.save(ticket);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new OptimisticLockConflictException("This ticket was updated by another user. Please reload and try again.");
        }

        slaRiskService.recalculateRisk(ticket);
        auditService.log("TICKET_UPDATED", "TICKET", ticket.getId(), ticket.getTicketNumber(), oldValue,
                "title=" + ticket.getTicketTitle() + ";priority=" + ticket.getPriority().getPriorityCode());
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketForView(Long ticketId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticketAccessGuard.assertCanView(ticket);
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> getMyTickets(Long developerId, Pageable pageable) {
        return ticketRepository.findByAssignedUser_IdAndActiveTrue(developerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> getAllTickets(String search, Long statusId, Long priorityId, Long customerId,
                                       Long developerId, String riskCategory, Pageable pageable) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (StringUtils.isNotBlank(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ticketNumber")), like),
                        cb.like(cb.lower(root.get("ticketTitle")), like),
                        cb.like(cb.lower(root.get("crmId")), like)
                ));
            }
            if (statusId != null) predicates.add(cb.equal(root.get("currentStatus").get("id"), statusId));
            if (priorityId != null) predicates.add(cb.equal(root.get("priority").get("id"), priorityId));
            if (customerId != null) predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            if (developerId != null) predicates.add(cb.equal(root.get("assignedUser").get("id"), developerId));
            if (StringUtils.isNotBlank(riskCategory)) predicates.add(cb.equal(root.get("slaRiskCategory"), riskCategory));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return ticketRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getMyOpenTickets(Long developerId) {
        return ticketRepository.findOpenActiveByDeveloper(developerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTicketNumberAvailable(String ticketNumber) {
        return !ticketRepository.existsByTicketNumberIgnoreCase(ticketNumber);
    }

    @Override
    @Transactional
    public Ticket changeStatus(Long ticketId, TicketStatusChangeRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticketAccessGuard.assertCanView(ticket);
        checkOptimisticLock(ticket, request.getExpectedVersion());

        StatusMaster previousStatus = ticket.getCurrentStatus();
        ticketWorkflowService.validateTransition(previousStatus, request);

        StatusMaster newStatus = statusMasterRepository.findByStatusCode(request.getTargetStatusCode())
                .orElseThrow(() -> new ResourceNotFoundException("Target status is not configured."));

        ticket.setCurrentStatus(newStatus);
        ticket.setLastUpdatedDate(LocalDateTime.now());
        ticket.setLastActivityDate(LocalDateTime.now());

        if (StatusMaster.ON_HOLD.equals(newStatus.getStatusCode())) {
            ticket.setOnHold(true);
            ticket.setHoldReason(request.getHoldReason());
        } else {
            ticket.setOnHold(false);
        }
        if (StatusMaster.MOVED_TO_PRODUCTION.equals(newStatus.getStatusCode())) {
            ticket.setActualProductionDate(request.getActualProductionDate());
            ticket.setClosedDate(LocalDateTime.now());
        }
        if (StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM.equals(newStatus.getStatusCode())
                || StatusMaster.REASSIGNED_TO_OTHER_TEAM.equals(newStatus.getStatusCode())) {
            ticket.setReassignmentCount(ticket.getReassignmentCount() + 1);
            if (request.getTargetTeamId() != null) {
                teamMasterRepository.findById(request.getTargetTeamId()).ifPresent(ticket::setDependencyTeam);
            }
        }

        try {
            ticket = ticketRepository.save(ticket);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new OptimisticLockConflictException("This ticket was updated by another user. Please reload and try again.");
        }

        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId()).orElse(null);
        TicketActivity activity = TicketActivity.builder()
                .ticket(ticket).previousStatus(previousStatus).newStatus(newStatus)
                .progressPercentage(request.getProgressPercentage())
                .workSummary("Status changed to " + newStatus.getDisplayName())
                .detailedRemark(request.getRemark())
                .updatedBy(currentUserEntity)
                .source(TicketActivity.SOURCE_MANUAL)
                .build();
        activity = ticketActivityRepository.save(activity);
        wsrService.recordFromActivity(activity);

        agingService.recalculateAging(ticket);
        slaRiskService.recalculateRisk(ticket);

        if (ticket.getAssignedUser() != null) {
            notificationService.notify(ticket.getAssignedUser(), Notification.STATUS_CHANGED,
                    "Status updated: " + ticket.getTicketNumber(),
                    "Ticket " + ticket.getTicketNumber() + " moved to " + newStatus.getDisplayName() + ".", ticket);
        }
        if (StatusMaster.MOVED_TO_PRODUCTION.equals(newStatus.getStatusCode())) {
            notifyAdmins(Notification.MOVED_TO_PRODUCTION, "Ticket moved to production: " + ticket.getTicketNumber(),
                    "Ticket " + ticket.getTicketNumber() + " has been moved to production.", ticket);
        }
        auditService.log("STATUS_CHANGED", "TICKET", ticket.getId(), ticket.getTicketNumber(),
                previousStatus.getStatusCode(), newStatus.getStatusCode());
        return ticket;
    }

    @Override
    @Transactional
    public Ticket reassignTicket(Long ticketId, TicketReassignRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        assertAdministrativeAccess();
        checkOptimisticLock(ticket, request.getExpectedVersion());

        if (request.getTargetUserId() == null && request.getTargetTeamId() == null) {
            throw new BusinessValidationException("A target developer or target team is required to reassign a ticket.");
        }

        User previousAssignee = ticket.getAssignedUser();
        User newAssignee = null;
        TeamMaster newTeam = null;
        if (request.getTargetUserId() != null) {
            newAssignee = userRepository.findById(request.getTargetUserId())
                    .filter(User::isActive)
                    .orElseThrow(() -> new BusinessValidationException("Assigned developer must be an active user."));
        }
        if (request.getTargetTeamId() != null) {
            newTeam = teamMasterRepository.findById(request.getTargetTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found."));
        }

        ticket.setPreviousAssignee(previousAssignee);
        ticket.setAssignedUser(newAssignee);
        ticket.setAssignmentDate(LocalDateTime.now());
        ticket.setReassignmentCount(ticket.getReassignmentCount() + 1);
        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId()).orElse(null);
        ticket.setAssignedBy(currentUserEntity);

        try {
            ticket = ticketRepository.save(ticket);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new OptimisticLockConflictException("This ticket was updated by another user. Please reload and try again.");
        }

        recordAssignment(ticket, previousAssignee, newAssignee, newTeam, TicketAssignment.REASSIGNMENT, request.getReason());
        agingService.recalculateAging(ticket);
        slaRiskService.recalculateRisk(ticket);

        if (newAssignee != null) {
            notificationService.notify(newAssignee, Notification.TICKET_REASSIGNED,
                    "Ticket reassigned to you: " + ticket.getTicketNumber(),
                    "Ticket " + ticket.getTicketNumber() + " has been reassigned to you. Reason: " + request.getReason(),
                    ticket);
        }
        auditService.log("TICKET_REASSIGNED", "TICKET", ticket.getId(), ticket.getTicketNumber(),
                previousAssignee != null ? previousAssignee.getUsername() : "UNASSIGNED",
                newAssignee != null ? newAssignee.getUsername() : "TEAM:" + (newTeam != null ? newTeam.getTeamCode() : ""));
        return ticket;
    }

    @Override
    @Transactional
    public Ticket addActivity(Long ticketId, TicketActivityRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticketAccessGuard.assertCanModifyAsDeveloper(ticket);

        ActivityTypeMaster activityType = activityTypeMasterRepository.findById(request.getActivityTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity type not found."));
        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessValidationException("Authentication is required."));

        TicketActivity activity = TicketActivity.builder()
                .ticket(ticket).previousStatus(ticket.getCurrentStatus()).newStatus(ticket.getCurrentStatus())
                .progressPercentage(request.getProgressPercentage())
                .activityType(activityType)
                .workSummary(request.getWorkSummary())
                .detailedRemark(request.getDetailedRemark())
                .blocker(request.getBlocker())
                .rootCause(request.getRootCause())
                .actionTaken(request.getActionTaken())
                .nextAction(request.getNextAction())
                .dependency(request.getDependency())
                .estimatedCompletionDate(request.getEstimatedCompletionDate())
                .hoursSpent(request.getHoursSpent())
                .updatedBy(currentUserEntity)
                .source(TicketActivity.SOURCE_MANUAL)
                .build();
        activity = ticketActivityRepository.save(activity);
        wsrService.recordFromActivity(activity);

        ticket.setLastActivityDate(LocalDateTime.now());
        ticket.setLastUpdatedDate(LocalDateTime.now());
        ticketRepository.save(ticket);

        slaRiskService.recalculateRisk(ticket);
        auditService.log("ACTIVITY_ADDED", "TICKET_ACTIVITY", activity.getId(), ticket.getTicketNumber(),
                null, request.getWorkSummary());
        return ticket;
    }

    @Override
    @Transactional
    public void addComment(Long ticketId, CommentRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticketAccessGuard.assertCanView(ticket);
        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessValidationException("Authentication is required."));

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .commentType(StringUtils.defaultIfBlank(request.getCommentType(), TicketComment.GENERAL))
                .commentText(request.getCommentText())
                .followUpDate(request.getFollowUpDate())
                .createdBy(currentUserEntity)
                .build();
        ticketCommentRepository.save(comment);

        if (ticket.getAssignedUser() != null && !ticket.getAssignedUser().getId().equals(currentUserEntity.getId())) {
            notificationService.notify(ticket.getAssignedUser(), Notification.REMARK_ADDED,
                    "New comment on " + ticket.getTicketNumber(), request.getCommentText(), ticket);
        }
        auditService.log("COMMENT_ADDED", "TICKET_COMMENT", comment.getId(), ticket.getTicketNumber(), null,
                request.getCommentText());
    }

    @Override
    @Transactional
    public void addFrontendChange(Long ticketId, FrontendChangeRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticketAccessGuard.assertCanModifyAsDeveloper(ticket);
        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessValidationException("Authentication is required."));

        FrontendChange change = FrontendChange.builder()
                .ticket(ticket)
                .screenFormName(request.getScreenFormName())
                .pageName(request.getPageName())
                .htmlFileName(request.getHtmlFileName())
                .jsFileName(request.getJsFileName())
                .cssFileName(request.getCssFileName())
                .functionName(request.getFunctionName())
                .uiComponent(request.getUiComponent())
                .validationChanged(request.getValidationChanged())
                .changeDescription(request.getChangeDescription())
                .reviewStatus(StringUtils.defaultIfBlank(request.getReviewStatus(), "PENDING"))
                .deploymentStatus(StringUtils.defaultIfBlank(request.getDeploymentStatus(), "PENDING"))
                .createdBy(currentUserEntity)
                .build();
        frontendChangeRepository.save(change);
        auditService.log("FRONTEND_CHANGE_ADDED", "FRONTEND_CHANGE", change.getId(), ticket.getTicketNumber(), null,
                request.getChangeDescription());
    }

    @Override
    @Transactional
    public void addBackendChange(Long ticketId, BackendChangeRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticketAccessGuard.assertCanModifyAsDeveloper(ticket);
        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessValidationException("Authentication is required."));

        if (StringUtils.isBlank(request.getJarName()) || StringUtils.isBlank(request.getPipelineName())
                || StringUtils.isBlank(request.getClassName()) || StringUtils.isBlank(request.getMethodName())) {
            throw new BusinessValidationException(
                    "JAR Name, Pipeline Name, Class Name and Method Name are mandatory for backend changes.");
        }

        BackendChange change = BackendChange.builder()
                .ticket(ticket)
                .applicationModule(request.getApplicationModule())
                .jarName(request.getJarName())
                .jarVersion(request.getJarVersion())
                .buildNumber(request.getBuildNumber())
                .pipelineName(request.getPipelineName())
                .pipelineReference(request.getPipelineReference())
                .packageName(request.getPackageName())
                .className(request.getClassName())
                .methodName(request.getMethodName())
                .apiServiceName(request.getApiServiceName())
                .changeDescription(request.getChangeDescription())
                .codeReviewStatus(StringUtils.defaultIfBlank(request.getCodeReviewStatus(), "PENDING"))
                .buildStatus(StringUtils.defaultIfBlank(request.getBuildStatus(), "PENDING"))
                .deploymentStatus(StringUtils.defaultIfBlank(request.getDeploymentStatus(), BackendChange.PENDING))
                .environment(request.getEnvironment())
                .rollbackRequired(request.isRollbackRequired())
                .rollbackStatus(request.getRollbackStatus())
                .rollbackRemark(request.getRollbackRemark())
                .createdBy(currentUserEntity)
                .build();
        backendChangeRepository.save(change);
        auditService.log("BACKEND_CHANGE_ADDED", "BACKEND_CHANGE", change.getId(), ticket.getTicketNumber(), null,
                request.getClassName() + "#" + request.getMethodName());
    }

    @Override
    @Transactional
    public void addDatabaseChange(Long ticketId, DatabaseChangeRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        ticketAccessGuard.assertCanModifyAsDeveloper(ticket);
        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessValidationException("Authentication is required."));

        DatabaseChange change = DatabaseChange.builder()
                .ticket(ticket)
                .databaseName(request.getDatabaseName())
                .schemaName(request.getSchemaName())
                .tableName(request.getTableName())
                .columnName(request.getColumnName())
                .procedureName(request.getProcedureName())
                .functionName(request.getFunctionName())
                .triggerName(request.getTriggerName())
                .viewName(request.getViewName())
                .queryChangeDescription(request.getQueryChangeDescription())
                .scriptFileName(request.getScriptFileName())
                .executionSequence(request.getExecutionSequence())
                .backupRequired(request.isBackupRequired())
                .rollbackScriptAvailable(request.isRollbackScriptAvailable())
                .dataMigrationRequired(request.isDataMigrationRequired())
                .environment(request.getEnvironment())
                .executionStatus(StringUtils.defaultIfBlank(request.getExecutionStatus(), "PENDING"))
                .validationResult(request.getValidationResult())
                .dbaRemark(request.getDbaRemark())
                .createdBy(currentUserEntity)
                .build();
        databaseChangeRepository.save(change);
        auditService.log("DATABASE_CHANGE_ADDED", "DATABASE_CHANGE", change.getId(), ticket.getTicketNumber(), null,
                request.getTableName());
    }

    private Ticket getTicketOrThrow(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found."));
    }

    private void assertAdministrativeAccess() {
        CustomUserDetails user = SecurityUtils.currentUser()
                .orElseThrow(() -> new BusinessValidationException("Authentication is required."));
        if (!ticketAccessGuard.hasAdministrativeAccess(user)) {
            throw new BusinessValidationException("You are not authorized to perform this action.");
        }
    }

    private void checkOptimisticLock(Ticket ticket, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(ticket.getVersion())) {
            throw new OptimisticLockConflictException(
                    "This ticket was updated by another user since you opened it. Please reload and try again.");
        }
    }

    private void recordAssignment(Ticket ticket, User from, User to, TeamMaster toTeam, String type, String reason) {
        User currentUserEntity = userRepository.findById(SecurityUtils.currentUserId()).orElse(ticket.getAssignedBy());
        TicketAssignment assignment = TicketAssignment.builder()
                .ticket(ticket).assignedFromUser(from).assignedToUser(to).assignedToTeam(toTeam)
                .assignedBy(currentUserEntity).assignmentType(type).reason(reason)
                .build();
        ticketAssignmentRepository.save(assignment);
    }

    private void notifyAdmins(String type, String title, String message, Ticket ticket) {
        List<User> teamLeads = userRepository.findByRoles_RoleCodeAndActiveTrue(Role.TEAM_LEAD);
        List<User> technicalLeads = userRepository.findByRoles_RoleCodeAndActiveTrue(Role.TECHNICAL_LEAD);
        teamLeads.forEach(u -> notificationService.notify(u, type, title, message, ticket));
        technicalLeads.forEach(u -> notificationService.notify(u, type, title, message, ticket));
    }
}
