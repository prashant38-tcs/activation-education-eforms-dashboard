package com.tcsion.eforms.workflow;

import com.tcsion.eforms.entity.StatusMaster;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class WorkflowTransitionConfig {

    private final Map<String, Set<String>> allowedTransitions = new HashMap<>();

    public WorkflowTransitionConfig() {
        allow(StatusMaster.NEW, StatusMaster.ASSIGNED, StatusMaster.WORK_IN_PROGRESS, StatusMaster.CANCELLED);
        allow(StatusMaster.ASSIGNED, StatusMaster.WORK_IN_PROGRESS, StatusMaster.ON_HOLD,
                StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM, StatusMaster.REASSIGNED_TO_OTHER_TEAM, StatusMaster.CANCELLED);
        allow(StatusMaster.WORK_IN_PROGRESS, StatusMaster.UAT_IN_PROGRESS, StatusMaster.QA_IN_PROGRESS,
                StatusMaster.ON_HOLD, StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM, StatusMaster.REASSIGNED_TO_OTHER_TEAM,
                StatusMaster.MOVED_TO_PRODUCTION, StatusMaster.CANCELLED);
        allow(StatusMaster.UAT_IN_PROGRESS, StatusMaster.QA_IN_PROGRESS, StatusMaster.WORK_IN_PROGRESS,
                StatusMaster.ON_HOLD, StatusMaster.MOVED_TO_PRODUCTION, StatusMaster.REOPENED);
        allow(StatusMaster.QA_IN_PROGRESS, StatusMaster.WORK_IN_PROGRESS, StatusMaster.ON_HOLD,
                StatusMaster.MOVED_TO_PRODUCTION, StatusMaster.REOPENED);
        allow(StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM, StatusMaster.WORK_IN_PROGRESS, StatusMaster.ON_HOLD,
                StatusMaster.REASSIGNED_TO_OTHER_TEAM, StatusMaster.CANCELLED);
        allow(StatusMaster.REASSIGNED_TO_OTHER_TEAM, StatusMaster.WORK_IN_PROGRESS, StatusMaster.ON_HOLD,
                StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM, StatusMaster.CANCELLED);
        allow(StatusMaster.ON_HOLD, StatusMaster.WORK_IN_PROGRESS, StatusMaster.UAT_IN_PROGRESS,
                StatusMaster.QA_IN_PROGRESS, StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM,
                StatusMaster.REASSIGNED_TO_OTHER_TEAM, StatusMaster.CANCELLED);
        allow(StatusMaster.REOPENED, StatusMaster.WORK_IN_PROGRESS, StatusMaster.ON_HOLD,
                StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM, StatusMaster.REASSIGNED_TO_OTHER_TEAM);
        allow(StatusMaster.MOVED_TO_PRODUCTION, StatusMaster.CLOSED, StatusMaster.REOPENED);
    }

    private void allow(String from, String... to) {
        allowedTransitions.put(from, new LinkedHashSet<>(Arrays.asList(to)));
    }

    public boolean isTransitionAllowed(String fromStatusCode, String toStatusCode) {
        if (fromStatusCode.equals(toStatusCode)) return true;
        Set<String> allowed = allowedTransitions.get(fromStatusCode);
        return allowed != null && allowed.contains(toStatusCode);
    }

    public Set<String> allowedTargets(String fromStatusCode) {
        return allowedTransitions.getOrDefault(fromStatusCode, Collections.emptySet());
    }
}
