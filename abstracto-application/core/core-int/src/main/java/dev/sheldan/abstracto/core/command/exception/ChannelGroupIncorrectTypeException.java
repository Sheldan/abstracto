package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.model.exception.ChannelGroupIncorrectTypeExceptionModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ChannelGroupIncorrectTypeException extends AbstractoRunTimeException implements Templatable {

    private final ChannelGroupIncorrectTypeExceptionModel model;

    public ChannelGroupIncorrectTypeException(String key, String correctType) {
        super("Channel group has the incorrect type.");
        this.model = ChannelGroupIncorrectTypeExceptionModel.builder().name(key).correctType(correctType).build();
    }
    @Override
    public String getTemplateName() {
        return "channel_group_incorrect_type_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
