package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;

public class AdminModeDetail implements ConditionDetail {
    @Override
    public String getTemplateName() {
        return "admin_mode_enabled_condition";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
