package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class BackendChangeRequest {
    private String applicationModule;
    @NotBlank(message = "JAR Name is required")
    private String jarName;
    private String jarVersion;
    private String buildNumber;
    @NotBlank(message = "Pipeline Name is required")
    private String pipelineName;
    private String pipelineReference;
    private String packageName;
    @NotBlank(message = "Class Name is required")
    private String className;
    @NotBlank(message = "Method Name is required")
    private String methodName;
    private String apiServiceName;
    private String changeDescription;
    private String codeReviewStatus;
    private String buildStatus;
    @NotBlank(message = "Deployment Status is required")
    private String deploymentStatus;
    private String environment;
    private boolean rollbackRequired;
    private String rollbackStatus;
    private String rollbackRemark;
}
