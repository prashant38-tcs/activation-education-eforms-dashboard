package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class TicketCreateRequest {
    @NotBlank(message = "Ticket Number is required")
    private String ticketNumber;
    private String crmId;
    @NotNull(message = "Customer is required")
    private Long customerId;
    @NotBlank(message = "Ticket Title is required")
    private String ticketTitle;
    private String ticketDescription;
    private String shortPlannedMilestone;
    @NotNull(message = "Ticket Type is required")
    private Long ticketTypeId;
    @NotNull(message = "Priority is required")
    private Long priorityId;
    @NotNull(message = "Severity is required")
    private Long severityId;
    private Long assignedUserId;
    private Long sourceTeamId;
    private Long dependencyTeamId;
    private LocalDate expectedClosureDate;
    private LocalDate estimatedProductionDate;
}
