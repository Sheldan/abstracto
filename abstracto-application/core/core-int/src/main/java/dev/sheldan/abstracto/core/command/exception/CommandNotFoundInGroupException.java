package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class CommandNotFoundInGroupException extends AbstractoRunTimeException implements Templatable {

    public CommandNotFoundInGroupException() {
        super("Command was not found in given group.");
    }

    @Override
    public String getTemplateName() {
        return "command_not_found_in_group_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
