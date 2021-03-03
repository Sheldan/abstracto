package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.AssignableRoleNotUsableExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.templating.Templatable;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Exception thrown in case the defined {@link net.dv8tion.jda.api.entities.Role role} cannot be interacted with by the bot,
 * because of permissions, this can happen if the role is the same or higher than the bot, therefore cannot be given to
 * {@link net.dv8tion.jda.api.entities.Member members}
 */
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
