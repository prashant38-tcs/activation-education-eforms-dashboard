package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrontendChangeRequest {
    private String screenFormName;
    private String pageName;
    private String htmlFileName;
    private String jsFileName;
    private String cssFileName;
    private String functionName;
    private String uiComponent;
    private String validationChanged;
    private String changeDescription;
    private String reviewStatus;
    private String deploymentStatus;
}
