package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;
import dev.sheldan.abstracto.core.command.model.condition.ImmuneUserConditionDetailModel;
import net.dv8tion.jda.api.entities.Role;

public class ImmuneUserConditionDetail implements ConditionDetail {

    private final ImmuneUserConditionDetailModel model;

    public ImmuneUserConditionDetail(Role role, String effectTypeKey) {
        this.model = ImmuneUserConditionDetailModel
                .builder()
                .role(role)
                .effectTypeKey(effectTypeKey)
                .build();
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
