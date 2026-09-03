package com.tcsion.eforms.workflow;

import com.tcsion.eforms.dto.request.TicketStatusChangeRequest;
import com.tcsion.eforms.entity.StatusMaster;
import com.tcsion.eforms.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketWorkflowServiceImpl implements TicketWorkflowService {

    private final WorkflowTransitionConfig transitionConfig;

    @Override
    public void validateTransition(StatusMaster currentStatus, TicketStatusChangeRequest request) {
        List<String> errors = new ArrayList<>();
        String from = currentStatus.getStatusCode();
        String to = request.getTargetStatusCode();

        if (StringUtils.isBlank(request.getRemark())) {
            errors.add("A remark is required for every status change.");
        }
        if (currentStatus.isTerminal()) {
            errors.add("Ticket is in a terminal status (" + currentStatus.getDisplayName() + ") and cannot be updated further.");
        } else if (!transitionConfig.isTransitionAllowed(from, to)) {
            errors.add("Status cannot move from '" + currentStatus.getDisplayName() + "' directly to '" + to + "'.");
        }

        switch (to) {
            case StatusMaster.MOVED_TO_PRODUCTION:
                if (request.getActualProductionDate() == null) {
                    errors.add("Actual Production Date is required to mark a ticket as Moved To Production.");
                }
                if (!request.isTechnicalReadinessConfirmed()) {
                    errors.add("Technical readiness must be confirmed before moving a ticket to production.");
                }
                break;
            case StatusMaster.ON_HOLD:
                if (StringUtils.isBlank(request.getHoldReason())) {
                    errors.add("Hold Reason is required when placing a ticket On Hold.");
                }
                break;
            case StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM:
                if (request.getTargetTeamId() == null) {
                    errors.add("Dependency team details are required when reassigning to the Framework Team.");
                }
                break;
            case StatusMaster.REASSIGNED_TO_OTHER_TEAM:
                if (request.getTargetTeamId() == null && request.getTargetAssigneeId() == null) {
                    errors.add("A target team or assignee is required when reassigning to another team.");
                }
                break;
            case StatusMaster.UAT_IN_PROGRESS:
                if (StringUtils.isBlank(request.getUatReference()) && StringUtils.isBlank(request.getRemark())) {
                    errors.add("A UAT reference or handover remark is required to move a ticket into UAT.");
                }
                break;
            case StatusMaster.QA_IN_PROGRESS:
                if (StringUtils.isBlank(request.getQaHandoverDetails()) && StringUtils.isBlank(request.getRemark())) {
                    errors.add("QA handover details are required to move a ticket into QA.");
                }
                break;
            default:
                break;
        }
        if (!errors.isEmpty()) {
            throw new BusinessValidationException(errors);
        }
    }
}
