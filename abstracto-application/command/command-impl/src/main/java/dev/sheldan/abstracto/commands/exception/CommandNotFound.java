package dev.sheldan.abstracto.commands.exception;

import dev.sheldan.abstracto.command.Templatable;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class CommandNotFound extends AbstractoRunTimeException implements Templatable {

    public CommandNotFound(String s) {
        super(s);
    }

    @Override
    public String getTemplateName() {
        return "command_not_found";
    }

    @Override
    public Object getTemplateModel() {
        return null;
    }
}
