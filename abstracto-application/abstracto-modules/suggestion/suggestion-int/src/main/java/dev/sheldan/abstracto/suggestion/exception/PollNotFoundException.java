package dev.sheldan.abstracto.suggestion.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.suggestion.model.exception.PollNotFoundExceptionModel;

public class PollNotFoundException extends AbstractoRunTimeException implements Templatable {
    private final PollNotFoundExceptionModel model;

    public PollNotFoundException(Long pollId) {
        super("Poll not found");
        this.model = PollNotFoundExceptionModel
                .builder()
                .pollId(pollId)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "poll_does_not_exist_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
