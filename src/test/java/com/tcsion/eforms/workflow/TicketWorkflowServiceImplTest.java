package com.tcsion.eforms.workflow;

import com.tcsion.eforms.dto.request.TicketStatusChangeRequest;
import com.tcsion.eforms.entity.StatusMaster;
import com.tcsion.eforms.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TicketWorkflowServiceImplTest {

    private TicketWorkflowServiceImpl workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new TicketWorkflowServiceImpl(new WorkflowTransitionConfig());
    }

    private StatusMaster status(String code, boolean terminal) {
        return StatusMaster.builder().statusCode(code).displayName(code).terminal(terminal).open(!terminal).build();
    }

    @Test
    void rejectsStatusChangeWithoutRemark() {
        StatusMaster current = status(StatusMaster.WORK_IN_PROGRESS, false);
        TicketStatusChangeRequest request = new TicketStatusChangeRequest();
        request.setTargetStatusCode(StatusMaster.UAT_IN_PROGRESS);
        request.setRemark("");
        request.setUatReference("UAT-REF-1");

        assertThrows(BusinessValidationException.class, () -> workflowService.validateTransition(current, request));
    }

    @Test
    void rejectsInvalidTransition() {
        StatusMaster current = status(StatusMaster.NEW, false);
        TicketStatusChangeRequest request = new TicketStatusChangeRequest();
        request.setTargetStatusCode(StatusMaster.MOVED_TO_PRODUCTION);
        request.setRemark("Trying to skip states");
        request.setActualProductionDate(LocalDate.now());
        request.setTechnicalReadinessConfirmed(true);

        assertThrows(BusinessValidationException.class, () -> workflowService.validateTransition(current, request));
    }

    @Test
    void requiresActualProductionDateAndReadinessForProductionMove() {
        StatusMaster current = status(StatusMaster.WORK_IN_PROGRESS, false);
        TicketStatusChangeRequest request = new TicketStatusChangeRequest();
        request.setTargetStatusCode(StatusMaster.MOVED_TO_PRODUCTION);
        request.setRemark("Ready to ship");

        assertThrows(BusinessValidationException.class, () -> workflowService.validateTransition(current, request));
    }

    @Test
    void allowsValidProductionMove() {
        StatusMaster current = status(StatusMaster.WORK_IN_PROGRESS, false);
        TicketStatusChangeRequest request = new TicketStatusChangeRequest();
        request.setTargetStatusCode(StatusMaster.MOVED_TO_PRODUCTION);
        request.setRemark("Ready to ship");
        request.setActualProductionDate(LocalDate.now());
        request.setTechnicalReadinessConfirmed(true);

        assertDoesNotThrow(() -> workflowService.validateTransition(current, request));
    }

    @Test
    void requiresHoldReasonForOnHold() {
        StatusMaster current = status(StatusMaster.WORK_IN_PROGRESS, false);
        TicketStatusChangeRequest request = new TicketStatusChangeRequest();
        request.setTargetStatusCode(StatusMaster.ON_HOLD);
        request.setRemark("Pausing work");

        assertThrows(BusinessValidationException.class, () -> workflowService.validateTransition(current, request));
    }

    @Test
    void rejectsTransitionFromTerminalStatus() {
        StatusMaster current = status(StatusMaster.CLOSED, true);
        TicketStatusChangeRequest request = new TicketStatusChangeRequest();
        request.setTargetStatusCode(StatusMaster.WORK_IN_PROGRESS);
        request.setRemark("Trying to reopen a closed ticket");

        assertThrows(BusinessValidationException.class, () -> workflowService.validateTransition(current, request));
    }
}
