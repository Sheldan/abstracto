package dev.sheldan.abstracto.suggestion.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class UnSuggestNotPossibleException extends AbstractoRunTimeException implements Templatable {
    public UnSuggestNotPossibleException() {
        super("Not possible to remove suggestion.");
    }

    @Override
    public String getTemplateName() {
        return "un_suggest_not_possible_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
