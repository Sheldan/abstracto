package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.model.exception.ChannelGroupExistsExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ChannelGroupExistsException extends AbstractoRunTimeException implements Templatable {

    private final ChannelGroupExistsExceptionModel model;

    public ChannelGroupExistsException(String name) {
        super("Channel group already exists");
        this.model = ChannelGroupExistsExceptionModel.builder().name(name).build();
    }

    @Override
    public String getTemplateName() {
        return "channel_group_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
