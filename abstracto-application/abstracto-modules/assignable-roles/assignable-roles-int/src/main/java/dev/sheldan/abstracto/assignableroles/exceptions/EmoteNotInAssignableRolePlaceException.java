package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.EmoteNotInAssignableRolePlaceModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.templating.Templatable;

public class EmoteNotInAssignableRolePlaceException extends AbstractoRunTimeException implements Templatable {

    private final EmoteNotInAssignableRolePlaceModel model;

    public EmoteNotInAssignableRolePlaceException(FullEmote emote, String placeName) {
        super("Emote not found in assignable role place");
        this.model = EmoteNotInAssignableRolePlaceModel.builder().emote(emote).placeName(placeName).build();
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
