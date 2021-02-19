package dev.sheldan.abstracto.moderation.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NoMessageFoundException extends AbstractoRunTimeException implements Templatable {
    public NoMessageFoundException() {
        super("No message found");
    }

    @Override
    public String getTemplateName() {
        return "no_message_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
