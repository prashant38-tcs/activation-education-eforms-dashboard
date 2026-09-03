package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class TicketReassignRequest {
    private Long targetUserId;
    private Long targetTeamId;
    @NotBlank(message = "A reason is required when reassigning a ticket")
    private String reason;
    private Long expectedVersion;
}
