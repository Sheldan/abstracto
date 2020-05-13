package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class ConfigurationKeyNotFoundException extends AbstractoRunTimeException implements Templatable {

    private String key;

    public ConfigurationKeyNotFoundException(String key) {
        super("");
        this.key = key;
    }

    @Override
    public String getTemplateName() {
        return "config_key_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("key", this.key);
        return param;
    }
}
