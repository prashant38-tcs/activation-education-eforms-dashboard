package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
public class TicketStatusChangeRequest {
    @NotBlank(message = "Target status is required")
    private String targetStatusCode;
    @NotBlank(message = "A remark is required for every status change")
    private String remark;
    private String holdReason;
    private Long targetTeamId;
    private Long targetAssigneeId;
    private String uatReference;
    private String qaHandoverDetails;
    private LocalDate actualProductionDate;
    private boolean technicalReadinessConfirmed;
    private Integer progressPercentage;
    private Long expectedVersion;
}
