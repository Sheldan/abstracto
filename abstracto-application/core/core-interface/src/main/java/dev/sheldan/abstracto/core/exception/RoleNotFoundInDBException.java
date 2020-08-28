package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.RoleNotFoundInDBExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

public class RoleNotFoundInDBException extends AbstractoRunTimeException implements Templatable {

    private final RoleNotFoundInDBExceptionModel model;

    public RoleNotFoundInDBException(Long roleId) {
        super("Role not found in database");
        this.model = RoleNotFoundInDBExceptionModel.builder().roleId(roleId).build();
    }

    @Override
    public String getTemplateName() {
        return "role_not_found_in_db_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
