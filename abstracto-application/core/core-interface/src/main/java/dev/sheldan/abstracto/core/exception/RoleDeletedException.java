package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.exception.RoleDeletedModel;
import dev.sheldan.abstracto.templating.Templatable;

public class RoleDeletedException extends AbstractoRunTimeException implements Templatable {

    private RoleDeletedModel model;

    public RoleDeletedException(ARole role) {
        super("Role has been marked as deleted and cannot be used.");
        this.model = RoleDeletedModel.builder().role(role).build();
    }

    @Override
    public String getTemplateName() {
        return "";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
