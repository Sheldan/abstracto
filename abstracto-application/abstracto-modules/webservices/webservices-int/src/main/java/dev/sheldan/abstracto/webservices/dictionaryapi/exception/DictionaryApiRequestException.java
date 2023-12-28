package dev.sheldan.abstracto.webservices.dictionaryapi.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class DictionaryApiRequestException extends AbstractoRunTimeException implements Templatable {

    public DictionaryApiRequestException(Integer responseCode) {
        super(String.format("Request failure towards dictionary api %s.", responseCode));
    }

    @Override
    public String getTemplateName() {
        return "dictionary_api_request_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
