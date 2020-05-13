package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class CommandNotFoundException extends AbstractoRunTimeException implements Templatable {
    public CommandNotFoundException() {
        super("");
    }

    @Override
    public String getTemplateName() {
        return "command_not_found_exception_text";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
