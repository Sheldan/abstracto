package dev.sheldan.abstracto.assignableroles.model.condition;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssignableRoleMinLevelResult implements AssignableRolePlaceConditionModel {
    private AssignableRoleMinLevelModel model;

    @Override
    public String getTemplateName() {
        return "assignable_role_condition_min_level";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
