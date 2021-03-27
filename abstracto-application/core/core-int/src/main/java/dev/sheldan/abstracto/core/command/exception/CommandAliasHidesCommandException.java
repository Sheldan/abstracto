package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.model.exception.CommandAliasHidesCommandModel;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class CommandAliasHidesCommandException extends AbstractoRunTimeException implements Templatable {

    private final CommandAliasHidesCommandModel model;

    public CommandAliasHidesCommandException(String existingCommand) {
        this.model = CommandAliasHidesCommandModel
                .builder()
                .existingCommand(existingCommand)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "command_in_server_alias_hides_command_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
