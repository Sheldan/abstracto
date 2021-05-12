package dev.sheldan.abstracto.webservices.urban.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.webservices.urban.model.exception.UrbanDictionaryRequestExceptionModel;

public class UrbanDictionaryRequestException extends AbstractoRunTimeException implements Templatable {

    private final UrbanDictionaryRequestExceptionModel model;

    public UrbanDictionaryRequestException(Integer responseCode) {
        super(String.format("Request failure towards urban dictionary %s.", responseCode));
        this.model = UrbanDictionaryRequestExceptionModel
                .builder()
                .responseCode(responseCode)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "urban_dictionary_request_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
