package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class AssignableRoleConditionAlreadyExistsException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "assignable_role_condition_already_present_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
