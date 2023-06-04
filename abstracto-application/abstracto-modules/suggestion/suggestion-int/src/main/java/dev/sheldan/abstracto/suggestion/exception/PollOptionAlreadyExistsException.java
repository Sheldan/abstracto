package dev.sheldan.abstracto.suggestion.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class PollOptionAlreadyExistsException extends AbstractoRunTimeException implements Templatable {

    public PollOptionAlreadyExistsException() {
        super("Poll option already exists.");
    }

    @Override
    public String getTemplateName() {
        return "poll_option_already_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
