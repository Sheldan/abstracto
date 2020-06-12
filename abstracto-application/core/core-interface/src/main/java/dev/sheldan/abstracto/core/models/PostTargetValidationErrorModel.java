package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class PostTargetValidationErrorModel implements ValidationErrorModel {

    private String postTargetName;

    @Override
    public String getTemplateName() {
        return "post_target_not_setup";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> params = new HashMap<>();
        params.put("postTargetKey", postTargetName);
        return params;
    }
}
