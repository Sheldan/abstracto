package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.assignableroles.model.exception.EmoteNotInAssignableRolePlaceExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * Exception which is thrown in case a given {@link dev.sheldan.abstracto.core.models.database.AEmote} was not found
 * in the {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace}, when it was tried to switch
 * or move the emotes around.
 */
public class EmoteNotInAssignableRolePlaceException extends AbstractoRunTimeException implements Templatable {

    private final EmoteNotInAssignableRolePlaceExceptionModel model;

    public EmoteNotInAssignableRolePlaceException(FullEmote emote, String placeName) {
        super("Emote not found in assignable role place");
        this.model = EmoteNotInAssignableRolePlaceExceptionModel.builder().emote(emote).placeName(placeName).build();
    }

    @Override
    public String getTemplateName() {
        return "emote_not_in_assignable_role_place_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
