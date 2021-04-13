package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ReactTooManyReactionsException extends AbstractoRunTimeException implements Templatable {

    public ReactTooManyReactionsException() {
        super("Adding reactions would lead to too many reactions.");
    }

    @Override
    public String getTemplateName() {
        return "react_too_many_reactions_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
