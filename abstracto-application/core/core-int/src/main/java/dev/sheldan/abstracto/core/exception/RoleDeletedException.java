package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.exception.RoleDeletedExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class RoleDeletedException extends AbstractoRunTimeException implements Templatable {

    private final RoleDeletedExceptionModel model;

    public RoleDeletedException(ARole role) {
        super("Role has been marked as deleted and cannot be used.");
        this.model = RoleDeletedExceptionModel.builder().role(role).build();
    }

    @Override
    public String getTemplateName() {
        return "role_disabled_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
