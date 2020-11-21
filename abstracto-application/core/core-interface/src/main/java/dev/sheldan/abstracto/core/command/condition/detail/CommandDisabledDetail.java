package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;

public class CommandDisabledDetail implements ConditionDetail {

    @Override
    public String getTemplateName() {
        return "command_disabled_condition";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
