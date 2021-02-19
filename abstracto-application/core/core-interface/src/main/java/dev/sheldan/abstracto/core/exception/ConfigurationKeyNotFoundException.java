package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.ConfigurationKeyNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class ConfigurationKeyNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final ConfigurationKeyNotFoundExceptionModel model;

    public ConfigurationKeyNotFoundException(String key) {
        super("Configuration key not found");
        this.model = ConfigurationKeyNotFoundExceptionModel.builder().key(key).build();
    }

    @Override
    public String getTemplateName() {
        return "config_key_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
       return model;
    }
}
