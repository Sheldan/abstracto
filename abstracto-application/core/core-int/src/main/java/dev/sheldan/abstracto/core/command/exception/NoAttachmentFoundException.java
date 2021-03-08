package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NoAttachmentFoundException extends AbstractoRunTimeException implements Templatable {

    public NoAttachmentFoundException() {
        super("No attachment was found in the message.");
    }

    @Override
    public String getTemplateName() {
        return "attachment_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
