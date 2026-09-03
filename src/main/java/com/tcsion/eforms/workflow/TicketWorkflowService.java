package com.tcsion.eforms.workflow;

import com.tcsion.eforms.dto.request.TicketStatusChangeRequest;
import com.tcsion.eforms.entity.StatusMaster;

public interface TicketWorkflowService {
    void validateTransition(StatusMaster currentStatus, TicketStatusChangeRequest request);
}
