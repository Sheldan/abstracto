package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class EmoteMissingValidationErrorModel implements ValidationErrorModel {

    private String emoteKey;

    @Override
    public String getTemplateName() {
        return "emote_not_setup";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> params = new HashMap<>();
        params.put("emoteKey", emoteKey);
        return params;
    }
}

