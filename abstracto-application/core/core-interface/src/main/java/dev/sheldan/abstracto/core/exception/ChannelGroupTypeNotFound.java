package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.ChannelGroupTypeNotFoundExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.List;

public class ChannelGroupTypeNotFound extends AbstractoRunTimeException implements Templatable {

    private final ChannelGroupTypeNotFoundExceptionModel model;

    public ChannelGroupTypeNotFound(List<String> channelGroupTypeKeys) {
        this.model = ChannelGroupTypeNotFoundExceptionModel.builder().availableGroupTypeKeys(channelGroupTypeKeys).build();
    }

    @Override
    public String getTemplateName() {
        return "channel_group_type_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return this.model;
    }
}
