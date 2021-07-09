package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.assignableroles.model.exception.AssignableRoleNotFoundExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class AssignableRoleNotFoundException extends AbstractoTemplatableException {

    private final AssignableRoleNotFoundExceptionModel model;

    public AssignableRoleNotFoundException(Long roleId) {
        super("Role to assign is not available anymore.");
        this.model = AssignableRoleNotFoundExceptionModel
                .builder()
                .roleId(roleId)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return this.model;
    }
}
