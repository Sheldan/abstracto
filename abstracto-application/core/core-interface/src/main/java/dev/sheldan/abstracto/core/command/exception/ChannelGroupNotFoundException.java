package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.ChannelGroupNotFoundExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.List;

public class ChannelGroupNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final ChannelGroupNotFoundExceptionModel model;

    public ChannelGroupNotFoundException(String key, List<String> available) {
        super("Channel group not found");
        this.model = ChannelGroupNotFoundExceptionModel.builder().name(key).available(available).build();
    }
    @Override
    public String getTemplateName() {
        return "channel_group_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
