package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.assignableroles.model.exception.AssignableRoleAlreadyDefinedExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.templating.Templatable;
import net.dv8tion.jda.api.entities.Role;

/**
 * Exception thrown in case the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRole} has already been
 * defined for an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace}. This is identified
 * via the {@link net.dv8tion.jda.api.entities.Emote} which is used to react.
 */
public class AssignableRoleAlreadyDefinedException extends AbstractoRunTimeException implements Templatable {
    private final AssignableRoleAlreadyDefinedExceptionModel model;

    public AssignableRoleAlreadyDefinedException(Role role, String placeName) {
        super("Assignable role already assigned");
        this.model = AssignableRoleAlreadyDefinedExceptionModel
                .builder()
                .roleDisplay(RoleDisplay.fromRole(role))
                .placeName(placeName)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_already_defined_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
