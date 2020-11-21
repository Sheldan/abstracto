package dev.sheldan.abstracto.modmail.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;

public class NotInModMailThreadConditionDetail implements ConditionDetail {

    @Override
    public String getTemplateName() {
        return "modmail_not_in_modmail_thread_condition";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
