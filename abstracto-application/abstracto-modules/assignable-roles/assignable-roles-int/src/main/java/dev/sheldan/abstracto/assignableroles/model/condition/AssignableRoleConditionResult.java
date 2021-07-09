package dev.sheldan.abstracto.assignableroles.model.condition;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssignableRoleConditionResult {
    private Boolean fulfilled;
    private AssignableRoleConditionType causingCondition;
    private AssignableRolePlaceConditionModel model;

    public static AssignableRoleConditionResult fromFail(AssignableRoleConditionType cause, AssignableRolePlaceConditionModel model) {
        return AssignableRoleConditionResult
                .builder()
                .causingCondition(cause)
                .model(model)
                .fulfilled(false)
                .build();
    }

    public static AssignableRoleConditionResult fromFail(AssignableRoleConditionType cause) {
        return AssignableRoleConditionResult
                .builder()
                .causingCondition(cause)
                .fulfilled(false)
                .build();
    }

    public static AssignableRoleConditionResult fromSuccess() {
        return AssignableRoleConditionResult
                .builder()
                .fulfilled(true)
                .build();
    }
}
