package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.CommandInMultipleChannelGroupsExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class CommandInMultipleChannelGroupsException extends AbstractoRunTimeException implements Templatable {

    private final CommandInMultipleChannelGroupsExceptionModel model;

    public CommandInMultipleChannelGroupsException(String channelGroupName) {
        super("Command is already in another group of this type.");
        this.model = CommandInMultipleChannelGroupsExceptionModel
                .builder()
                .channelGroupName(channelGroupName)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "command_in_multiple_channel_groups_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
