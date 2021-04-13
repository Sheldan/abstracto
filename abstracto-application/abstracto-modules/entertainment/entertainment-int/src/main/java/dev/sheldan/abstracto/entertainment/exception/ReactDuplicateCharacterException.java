package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ReactDuplicateCharacterException extends AbstractoRunTimeException implements Templatable {

    public ReactDuplicateCharacterException() {
        super("Could not replace all characters to be duplicate free.");
    }

    @Override
    public String getTemplateName() {
        return "react_duplicate_character_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
