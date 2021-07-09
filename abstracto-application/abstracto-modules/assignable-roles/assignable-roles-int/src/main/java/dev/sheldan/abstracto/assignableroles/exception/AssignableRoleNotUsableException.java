package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.assignableroles.model.exception.AssignableRoleNotUsableExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.templating.Templatable;
import net.dv8tion.jda.api.entities.Role;

/**
 * Exception thrown in case the defined {@link net.dv8tion.jda.api.entities.Role role} cannot be interacted with by the bot,
 * because of permissions, this can happen if the role is the same or higher than the bot, therefore cannot be given to
 * {@link net.dv8tion.jda.api.entities.Member members}
 */
public class AssignableRoleNotUsableException extends AbstractoRunTimeException implements Templatable {
    private final AssignableRoleNotUsableExceptionModel model;

    public AssignableRoleNotUsableException(Role role) {
        super("Role is not usable as assignable role");
        this.model = AssignableRoleNotUsableExceptionModel
                .builder()
                .roleDisplay(RoleDisplay.fromRole(role))
                .build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_not_usable_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
