package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;
import dev.sheldan.abstracto.core.command.execution.CoolDownCheckResult;
import dev.sheldan.abstracto.core.command.model.condition.CommandCoolDownDetailModel;

public class CommandCoolDownDetail implements ConditionDetail {

    private final CommandCoolDownDetailModel model;

    public CommandCoolDownDetail(CoolDownCheckResult result) {
        this.model = CommandCoolDownDetailModel.builder().reason(result).build();
    }

    @Override
    public String getTemplateName() {
        return "command_cool_down_detail";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
