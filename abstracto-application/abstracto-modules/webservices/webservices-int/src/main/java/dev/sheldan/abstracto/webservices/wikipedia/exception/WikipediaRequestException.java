package dev.sheldan.abstracto.webservices.wikipedia.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class WikipediaRequestException extends AbstractoRunTimeException implements Templatable {

    public WikipediaRequestException(Integer responseCode) {
        super(String.format("Request failure towards wikipedia %s.", responseCode));
    }

    @Override
    public String getTemplateName() {
        return "wikipedia_request_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
