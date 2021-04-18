package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.ChannelInMultipleChannelGroupsExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ChannelInMultipleChannelGroupsException extends AbstractoRunTimeException implements Templatable {

    private final ChannelInMultipleChannelGroupsExceptionModel model;

    public ChannelInMultipleChannelGroupsException(String channelGroupName) {
        super("Channel is already in another group of this type.");
        this.model = ChannelInMultipleChannelGroupsExceptionModel
                .builder()
                .channelGroupName(channelGroupName)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "channel_in_multiple_channel_groups_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
