package dev.sheldan.abstracto.assignableroles.model.template.condition;


public class AssignableRoleMinLevelDisplay implements AssignableRoleConditionDisplay {

    private final AssignableRoleMinLevelDisplayModel model;

    public AssignableRoleMinLevelDisplay(Integer level) {
        this.model = AssignableRoleMinLevelDisplayModel
                .builder()
                .minLevel(level)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_condition_display_min_level";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
