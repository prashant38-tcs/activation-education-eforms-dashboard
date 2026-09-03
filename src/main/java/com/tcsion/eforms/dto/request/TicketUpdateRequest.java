package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class TicketUpdateRequest {
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
    private Long dependencyTeamId;
    private LocalDate expectedClosureDate;
    private LocalDate estimatedProductionDate;
    @NotNull(message = "Version is required to prevent conflicting concurrent updates")
    private Long expectedVersion;
}
