package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class AssignableRoleConditionValueNotUsableException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "assignable_role_condition_value_not_usable_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
