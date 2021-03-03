package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.AssignableRoleAlreadyDefinedExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * Exception thrown in case the {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRole} has already been
 * defined for an {@link dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace}. This is identified
 * via the {@link net.dv8tion.jda.api.entities.Emote} which is used to react.
 */
public class AssignableRoleAlreadyDefinedException extends AbstractoRunTimeException implements Templatable {
    private final AssignableRoleAlreadyDefinedExceptionModel model;

    public AssignableRoleAlreadyDefinedException(FullEmote emote, String placeName) {
        super("Assignable role already assigned");
        this.model = AssignableRoleAlreadyDefinedExceptionModel.builder().emote(emote).placeName(placeName).build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_emote_already_defined_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
