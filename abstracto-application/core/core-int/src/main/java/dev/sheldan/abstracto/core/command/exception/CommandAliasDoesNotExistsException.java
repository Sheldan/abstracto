package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class CommandAliasDoesNotExistsException extends AbstractoRunTimeException implements Templatable {

    public CommandAliasDoesNotExistsException() {
        super("Command Alias does not exist.");
    }

    @Override
    public String getTemplateName() {
        return "command_in_server_alias_not_exists_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
