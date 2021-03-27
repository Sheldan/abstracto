package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.model.exception.ChannelNotInChannelGroupExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ChannelNotInChannelGroupException extends AbstractoRunTimeException implements Templatable {

    private final ChannelNotInChannelGroupExceptionModel model;

    public ChannelNotInChannelGroupException(AChannel channel, AChannelGroup group) {
        super("Channel is already part of channel group");
        this.model = ChannelNotInChannelGroupExceptionModel.builder().channel(channel).channelGroup(group).build();
    }

    @Override
    public String getTemplateName() {
        return "channel_not_in_channel_group_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
