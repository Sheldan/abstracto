package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class AssignableRolePlaceMaximumRolesException extends AbstractoTemplatableException {

    public AssignableRolePlaceMaximumRolesException() {
        super("The maximum amount of assignable roles have been reached.");
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_maximum_roles_reached_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
