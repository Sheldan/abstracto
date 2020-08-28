package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.InsufficientPermissionExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class InsufficientPermissionException extends AbstractoRunTimeException implements Templatable {

    private final InsufficientPermissionExceptionModel model;

    public InsufficientPermissionException(List<Role> allowedRoles) {
        super("Insufficient permissions, required role not given.");
        this.model = InsufficientPermissionExceptionModel.builder().allowedRoles(allowedRoles).build();
    }

    @Override
    public String getTemplateName() {
        return "insufficient_role_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
