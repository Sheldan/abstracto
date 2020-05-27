package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class DurationFormatException extends AbstractoRunTimeException implements Templatable {

    private final String invalidFormat;
    private final List<String> validFormats;

    public DurationFormatException(String wrongFormat, List<String> validFormats) {
        super("");
        this.invalidFormat = wrongFormat;
        this.validFormats = validFormats;
    }

    @Override
    public String getTemplateName() {
        return "duration_invalid_time_format_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("format", this.invalidFormat);
        param.put("valid", String.join(", ", this.validFormats));
        return param;
    }
}
