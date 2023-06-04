package dev.sheldan.abstracto.suggestion.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class PollCancellationNotPossibleException extends AbstractoRunTimeException implements Templatable {
    public PollCancellationNotPossibleException() {
        super("Not possible to cancel poll.");
    }

    @Override
    public String getTemplateName() {
        return "poll_cancellation_not_possible_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}

