package dev.sheldan.abstracto.assignableroles.exception;

import dev.sheldan.abstracto.assignableroles.model.exception.AssignableRolePlaceChannelDoesNotExistExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * Exception thrown in case the {@link dev.sheldan.abstracto.core.models.database.AChannel channel} in which a
 * {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place} is defined, does not exist
 */
public class AssignableRolePlaceChannelDoesNotExistException extends AbstractoRunTimeException implements Templatable {

    private final AssignableRolePlaceChannelDoesNotExistExceptionModel model;

    public AssignableRolePlaceChannelDoesNotExistException(Long channelId, String placeName) {
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
