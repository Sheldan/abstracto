package dev.sheldan.abstracto.webservices.dictionaryapi.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NoDictionaryApiDefinitionFoundException extends AbstractoRunTimeException implements Templatable {
    public NoDictionaryApiDefinitionFoundException() {
        super("No dictionary api definition found.");
    }

    @Override
    public String getTemplateName() {
        return "no_dictionary_api_definition_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
