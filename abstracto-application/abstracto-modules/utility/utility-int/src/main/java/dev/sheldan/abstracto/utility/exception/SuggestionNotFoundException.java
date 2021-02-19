package dev.sheldan.abstracto.utility.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.utility.models.exception.SuggestionNotFoundExceptionModel;

public class SuggestionNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final SuggestionNotFoundExceptionModel model;

    public SuggestionNotFoundException(Long suggestionId) {
        super("Suggestion not found");
        this.model = SuggestionNotFoundExceptionModel.builder().suggestionId(suggestionId).build();
    }

    @Override
    public String getTemplateName() {
        return "suggestion_does_not_exist_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
