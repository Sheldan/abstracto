package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class CommandNotFoundException extends AbstractoRunTimeException implements Templatable {
    public CommandNotFoundException() {
        super("Command not found");
    }

    @Override
    public String getTemplateName() {
        return "command_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
