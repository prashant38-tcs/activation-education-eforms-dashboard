package com.tcsion.eforms.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseChangeRequest {
    private String databaseName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String procedureName;
    private String functionName;
    private String triggerName;
    private String viewName;
    private String queryChangeDescription;
    private String scriptFileName;
    private Integer executionSequence;
    private boolean backupRequired;
    private boolean rollbackScriptAvailable;
    private boolean dataMigrationRequired;
    private String environment;
    private String executionStatus;
    private String validationResult;
    private String dbaRemark;
}
