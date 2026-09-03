package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TicketActivityRequest {
    @NotNull(message = "Activity Type is required")
    private Long activityTypeId;
    private String workSummary;
    private String detailedRemark;
    private String blocker;
    private String rootCause;
    private String actionTaken;
    private String nextAction;
    private String dependency;
    private LocalDate estimatedCompletionDate;
    private BigDecimal hoursSpent;
    private Integer progressPercentage;
}
