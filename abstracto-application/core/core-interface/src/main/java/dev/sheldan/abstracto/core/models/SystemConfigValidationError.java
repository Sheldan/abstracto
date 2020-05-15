package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class SystemConfigValidationError implements ValidationError {

    private String configKey;

    @Override
    public String getTemplateName() {
        return "config_key_not_setup";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> params = new HashMap<>();
        params.put("configKey", configKey);
        return params;
    }
}
