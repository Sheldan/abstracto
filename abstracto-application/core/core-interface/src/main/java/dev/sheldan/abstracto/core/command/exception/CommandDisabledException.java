package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class CommandDisabledException extends AbstractoRunTimeException implements Templatable {

    @Override
    public String getTemplateName() {
        return "command_disabled_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
