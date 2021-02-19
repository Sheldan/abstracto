package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.AssignableRoleNotUsableExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.templating.Templatable;
import net.dv8tion.jda.api.entities.Guild;

public class AssignableRoleNotUsableException extends AbstractoRunTimeException implements Templatable {
    private final AssignableRoleNotUsableExceptionModel model;

    public AssignableRoleNotUsableException(FullRole role, Guild guild) {
        super("Role is not usable as assignable role");
        this.model = AssignableRoleNotUsableExceptionModel.builder().role(role).guild(guild).build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_role_not_usable_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
