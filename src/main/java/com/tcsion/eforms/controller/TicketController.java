package com.tcsion.eforms.controller;

import com.tcsion.eforms.dto.request.*;
import com.tcsion.eforms.dto.response.ApiResponse;
import com.tcsion.eforms.entity.*;
import com.tcsion.eforms.exception.BusinessValidationException;
import com.tcsion.eforms.exception.ResourceNotFoundException;
import com.tcsion.eforms.repository.*;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.security.TicketAccessGuard;
import com.tcsion.eforms.service.AuditService;
import com.tcsion.eforms.service.FileStorageService;
import com.tcsion.eforms.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.InputStream;

@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketAccessGuard ticketAccessGuard;
    private final CustomerMasterRepository customerMasterRepository;
    private final TicketTypeMasterRepository ticketTypeMasterRepository;
    private final PriorityMasterRepository priorityMasterRepository;
    private final SeverityMasterRepository severityMasterRepository;
    private final StatusMasterRepository statusMasterRepository;
    private final ActivityTypeMasterRepository activityTypeMasterRepository;
    private final TeamMasterRepository teamMasterRepository;
    private final UserRepository userRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final FrontendChangeRepository frontendChangeRepository;
    private final BackendChangeRepository backendChangeRepository;
    private final DatabaseChangeRepository databaseChangeRepository;
    private final TicketAttachmentRepository ticketAttachmentRepository;
    private final AttachmentCategoryMasterRepository attachmentCategoryMasterRepository;
    private final DeploymentEnvironmentMasterRepository deploymentEnvironmentMasterRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    @GetMapping("/my")
    public String myTickets(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("lastActivityDate").descending());
        Page<Ticket> tickets = ticketService.getMyTickets(SecurityUtils.currentUserId(), pageable);
        model.addAttribute("tickets", tickets);
        model.addAttribute("pageTitle", "My Tickets");
        return "tickets/my-tickets";
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
    public String allTickets(@RequestParam(required = false) String search,
                              @RequestParam(required = false) Long statusId,
                              @RequestParam(required = false) Long priorityId,
                              @RequestParam(required = false) Long customerId,
                              @RequestParam(required = false) Long developerId,
                              @RequestParam(required = false) String riskCategory,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by("createdDate").descending());
        Page<Ticket> tickets = ticketService.getAllTickets(search, statusId, priorityId, customerId, developerId, riskCategory, pageable);
        model.addAttribute("tickets", tickets);
        model.addAttribute("customers", customerMasterRepository.findByActiveTrue());
        model.addAttribute("statuses", statusMasterRepository.findByActiveTrueOrderBySortOrderAsc());
        model.addAttribute("priorities", priorityMasterRepository.findByActiveTrueOrderByRankOrderAsc());
        model.addAttribute("developers", userRepository.findByRoles_RoleCodeAndActiveTrue(Role.DEVELOPER));
        model.addAttribute("pageTitle", "All Tickets");
        return "tickets/all-tickets";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
    public String createForm(Model model) {
        model.addAttribute("ticketCreateRequest", new TicketCreateRequest());
        addLookups(model);
        return "tickets/ticket-form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
    public String create(@Valid @ModelAttribute TicketCreateRequest ticketCreateRequest, Model model) {
        Ticket ticket = ticketService.createTicket(ticketCreateRequest);
        return "redirect:/tickets/" + ticket.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Ticket ticket = ticketService.getTicketForView(id);
        model.addAttribute("ticket", ticket);
        model.addAttribute("activities", ticketActivityRepository.findByTicket_IdOrderByActivityDatetimeDesc(id));
        model.addAttribute("comments", ticketCommentRepository.findByTicket_IdOrderByCreatedAtAsc(id));
        model.addAttribute("frontendChanges", frontendChangeRepository.findByTicket_IdOrderByCreatedAtDesc(id));
        model.addAttribute("backendChanges", backendChangeRepository.findByTicket_IdOrderByCreatedAtDesc(id));
        model.addAttribute("databaseChanges", databaseChangeRepository.findByTicket_IdOrderByCreatedAtDesc(id));
        model.addAttribute("attachments", ticketAttachmentRepository.findByTicket_IdAndActiveTrueOrderByUploadedAtDesc(id));
        model.addAttribute("activityTypes", activityTypeMasterRepository.findByActiveTrue());
        model.addAttribute("teams", teamMasterRepository.findByActiveTrue());
        model.addAttribute("developers", userRepository.findByRoles_RoleCodeAndActiveTrue(Role.DEVELOPER));
        model.addAttribute("environments", deploymentEnvironmentMasterRepository.findByActiveTrueOrderBySortOrderAsc());
        model.addAttribute("attachmentCategories", attachmentCategoryMasterRepository.findByActiveTrue());
        model.addAttribute("isAdmin", ticketAccessGuard.hasAdministrativeAccess(
                SecurityUtils.currentUser().orElseThrow(() -> new BusinessValidationException("Authentication required"))));
        model.addAttribute("statusChangeRequest", new TicketStatusChangeRequest());
        return "tickets/ticket-detail";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @ModelAttribute TicketStatusChangeRequest request) {
        ticketService.changeStatus(id, request);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
    public String reassign(@PathVariable Long id, @ModelAttribute TicketReassignRequest request) {
        ticketService.reassignTicket(id, request);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/activity")
    public String addActivity(@PathVariable Long id, @ModelAttribute TicketActivityRequest request) {
        ticketService.addActivity(id, request);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id, @ModelAttribute CommentRequest request) {
        ticketService.addComment(id, request);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/frontend-change")
    public String addFrontendChange(@PathVariable Long id, @ModelAttribute FrontendChangeRequest request) {
        ticketService.addFrontendChange(id, request);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/backend-change")
    public String addBackendChange(@PathVariable Long id, @ModelAttribute BackendChangeRequest request) {
        ticketService.addBackendChange(id, request);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/database-change")
    public String addDatabaseChange(@PathVariable Long id, @ModelAttribute DatabaseChangeRequest request) {
        ticketService.addDatabaseChange(id, request);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/attachments")
    public String uploadAttachment(@PathVariable Long id, @RequestParam MultipartFile file,
                                    @RequestParam(required = false) Long categoryId) {
        Ticket ticket = ticketService.getTicketForView(id);
        String storedFileName = fileStorageService.store(file, "ticket-" + id);
        AttachmentCategoryMaster category = categoryId != null
                ? attachmentCategoryMasterRepository.findById(categoryId).orElse(null) : null;
        User currentUser = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessValidationException("Authentication required"));

        TicketAttachment attachment = TicketAttachment.builder()
                .ticket(ticket).category(category)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .storagePath("ticket-" + id)
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .uploadedBy(currentUser)
                .build();
        ticketAttachmentRepository.save(attachment);
        auditService.log("ATTACHMENT_UPLOADED", "TICKET_ATTACHMENT", attachment.getId(), ticket.getTicketNumber(),
                null, file.getOriginalFilename());
        return "redirect:/tickets/" + id;
    }

    @GetMapping("/{id}/attachments/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        Ticket ticket = ticketService.getTicketForView(id);
        TicketAttachment attachment = ticketAttachmentRepository.findById(attachmentId)
                .filter(a -> a.getTicket().getId().equals(ticket.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found."));
        InputStream in = fileStorageService.retrieve(attachment.getStoredFileName(), attachment.getStoragePath());
        auditService.log("ATTACHMENT_DOWNLOADED", "TICKET_ATTACHMENT", attachment.getId(), ticket.getTicketNumber(),
                null, attachment.getOriginalFileName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    @GetMapping("/api/check-ticket-number")
    @ResponseBody
    public ApiResponse<Boolean> checkTicketNumber(@RequestParam String ticketNumber) {
        return ApiResponse.ok(ticketService.isTicketNumberAvailable(ticketNumber));
    }

    private void addLookups(Model model) {
        model.addAttribute("customers", customerMasterRepository.findByActiveTrue());
        model.addAttribute("ticketTypes", ticketTypeMasterRepository.findByActiveTrue());
        model.addAttribute("priorities", priorityMasterRepository.findByActiveTrueOrderByRankOrderAsc());
        model.addAttribute("severities", severityMasterRepository.findByActiveTrueOrderByRankOrderAsc());
        model.addAttribute("developers", userRepository.findByRoles_RoleCodeAndActiveTrue(Role.DEVELOPER));
        model.addAttribute("teams", teamMasterRepository.findByActiveTrue());
    }
}
