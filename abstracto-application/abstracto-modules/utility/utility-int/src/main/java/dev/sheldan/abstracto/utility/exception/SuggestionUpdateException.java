package dev.sheldan.abstracto.utility.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class SuggestionUpdateException extends AbstractoRunTimeException implements Templatable {
    public SuggestionUpdateException() {
        super("Not possible to update suggestion.");
    }

    @Override
    public String getTemplateName() {
        return "suggestion_update_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
