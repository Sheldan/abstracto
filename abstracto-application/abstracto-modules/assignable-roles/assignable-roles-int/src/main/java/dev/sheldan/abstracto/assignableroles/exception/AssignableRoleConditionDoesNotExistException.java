package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class AssignableRoleConditionDoesNotExistException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "assignable_role_condition_does_not_exist_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
