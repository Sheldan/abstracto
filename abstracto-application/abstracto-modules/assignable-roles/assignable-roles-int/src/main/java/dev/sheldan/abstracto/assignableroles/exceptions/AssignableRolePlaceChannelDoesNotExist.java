package dev.sheldan.abstracto.assignableroles.exceptions;

import dev.sheldan.abstracto.assignableroles.models.exception.AssignableRolePlaceChannelDoesNotExistExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class AssignableRolePlaceChannelDoesNotExist extends AbstractoRunTimeException implements Templatable {

    private final AssignableRolePlaceChannelDoesNotExistExceptionModel model;

    public AssignableRolePlaceChannelDoesNotExist(Long channelId, String placeName) {
        super("Assignable role place channel does not exist");
        this.model = AssignableRolePlaceChannelDoesNotExistExceptionModel.builder().channelId(channelId).placeName(placeName).build();
    }

    @Override
    public String getTemplateName() {
        return "assignable_role_place_channel_does_not_exist_exception";
    }

    @Override
    public Object getTemplateModel() {
        return this.model;
    }
}
