package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.InstantFormatExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class InstantFormatException extends AbstractoRunTimeException implements Templatable {

    private final InstantFormatExceptionModel model;


    public InstantFormatException(String invalidFormat) {
        super("Instant format exception ");
        this.model = InstantFormatExceptionModel
                .builder()
                .invalidFormat(invalidFormat)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "instant_invalid_time_format_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
