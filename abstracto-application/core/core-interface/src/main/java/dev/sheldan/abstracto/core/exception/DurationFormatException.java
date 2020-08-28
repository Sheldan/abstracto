package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.DurationFormatExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.List;

public class DurationFormatException extends AbstractoRunTimeException implements Templatable {

    private final DurationFormatExceptionModel model;


    public DurationFormatException(String wrongFormat, List<String> validFormats) {
        super("Duration format exception ");
        this.model = DurationFormatExceptionModel.builder().invalidFormat(wrongFormat).validFormats(validFormats).build();
    }

    @Override
    public String getTemplateName() {
        return "duration_invalid_time_format_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
