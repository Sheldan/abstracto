package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.ImmuneUserExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;
import net.dv8tion.jda.api.entities.Role;

public class ImmuneUserException extends AbstractoRunTimeException implements Templatable {

    private final transient ImmuneUserExceptionModel model;

    public ImmuneUserException(Role role) {
        super("User is immune against the command");
        this.model = ImmuneUserExceptionModel.builder().role(role).build();
    }

    @Override
    public String getTemplateName() {
        return "immune_role_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
