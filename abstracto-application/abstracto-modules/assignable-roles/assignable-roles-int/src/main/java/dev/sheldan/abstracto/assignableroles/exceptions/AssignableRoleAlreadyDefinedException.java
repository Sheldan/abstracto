package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.AssignableRoleAlreadyDefinedModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.templating.Templatable;

public class AssignableRoleAlreadyDefinedException extends AbstractoRunTimeException implements Templatable {
    private final AssignableRoleAlreadyDefinedModel model;

    public AssignableRoleAlreadyDefinedException(FullEmote emote, String placeName) {
        super("Assignable role already assigned");
        this.model = AssignableRoleAlreadyDefinedModel.builder().emote(emote).placeName(placeName).build();
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
