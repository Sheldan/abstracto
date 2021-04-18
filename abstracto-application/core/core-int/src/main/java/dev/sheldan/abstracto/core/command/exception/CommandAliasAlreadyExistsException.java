package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.model.exception.CommandAliasAlreadyExistsModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class CommandAliasAlreadyExistsException extends AbstractoRunTimeException implements Templatable {

    private final CommandAliasAlreadyExistsModel model;

    public CommandAliasAlreadyExistsException(String existingCommand) {
        super("Command alias already exists.");
        this.model = CommandAliasAlreadyExistsModel
                .builder()
                .existingCommand(existingCommand)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "command_in_server_alias_already_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
