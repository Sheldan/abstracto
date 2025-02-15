package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;

public class NotBotOwnerConditionDetail implements ConditionDetail {


    public NotBotOwnerConditionDetail() {
    }

    @Override
    public String getTemplateName() {
        return "is_bot_owner_condition";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
