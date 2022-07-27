package dev.sheldan.abstracto.webservices.common.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.webservices.common.model.exception.SuggestQueriesExceptionModel;

public class SuggestQueriesException extends AbstractoRunTimeException implements Templatable {

    private final SuggestQueriesExceptionModel model;

    public SuggestQueriesException(Integer responseCode) {
        super(String.format("Request failure towards suggest queries %s.", responseCode));
        this.model = SuggestQueriesExceptionModel
                .builder()
                .responseCode(responseCode)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "suggest_queries_request_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
