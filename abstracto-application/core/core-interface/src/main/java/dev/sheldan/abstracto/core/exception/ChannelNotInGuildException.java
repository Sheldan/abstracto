package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.ChannelNotFoundExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

public class ChannelNotInGuildException extends AbstractoRunTimeException implements Templatable {

    private final ChannelNotFoundExceptionModel model;

    public ChannelNotInGuildException(Long channelId) {
        super("Channel not found in guild");
        this.model = ChannelNotFoundExceptionModel.builder().channelId(channelId).build();
    }

    @Override
    public String getTemplateName() {
            return "channel_not_in_guild_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
