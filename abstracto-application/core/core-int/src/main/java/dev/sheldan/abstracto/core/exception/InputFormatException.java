package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.InputFormatExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class InputFormatException extends AbstractoRunTimeException implements Templatable {

    private final InputFormatExceptionModel model;


    public InputFormatException(String wrongFormat, String validFormat) {
        super("Input format exception ");
        this.model = InputFormatExceptionModel
                .builder()
                .invalidFormat(wrongFormat)
                .validFormat(validFormat)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "input_invalid_format_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
