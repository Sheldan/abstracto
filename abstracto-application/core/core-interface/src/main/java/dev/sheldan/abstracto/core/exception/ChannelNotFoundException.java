package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.ChannelNotFoundExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

public class ChannelNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final ChannelNotFoundExceptionModel model;

    public ChannelNotFoundException(Long channelId) {
        super("Channel not found in database");
        this.model = ChannelNotFoundExceptionModel.builder().channelId(channelId).build();
    }

    @Override
    public String getTemplateName() {
        return "channel_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
