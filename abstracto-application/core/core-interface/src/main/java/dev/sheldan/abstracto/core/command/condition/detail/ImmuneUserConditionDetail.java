package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;
import dev.sheldan.abstracto.core.command.models.condition.ImmuneUserConditionDetailModel;
import net.dv8tion.jda.api.entities.Role;

public class ImmuneUserConditionDetail implements ConditionDetail {

    private final ImmuneUserConditionDetailModel model;

    public ImmuneUserConditionDetail(Role role) {
        this.model = ImmuneUserConditionDetailModel.builder().role(role).build();
    }

    @Override
    public String getTemplateName() {
        return "immune_role_condition";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
