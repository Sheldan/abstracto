package dev.sheldan.abstracto.utility.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class SuggestionNotFoundException extends AbstractoRunTimeException implements Templatable {

    private Long suggestionId;
    public SuggestionNotFoundException(Long suggestionId) {
        super("");
        this.suggestionId = suggestionId;
    }

    @Override
    public String getTemplateName() {
        return "suggestion_does_not_exist_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> params = new HashMap<>();
        params.put("id", this.suggestionId);
        return params;
    }
}
