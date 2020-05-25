package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class SystemConfigStepException extends AbstractoRunTimeException implements Templatable {


    public SystemConfigStepException(Throwable cause) {
        super("", cause);
    }

    @Override
    public String getTemplateName() {
        return "setup_system_config_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("message", getCause().getMessage());

        return stringStringHashMap;
    }
}
