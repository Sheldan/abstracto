package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class EmoteNotDefinedException extends AbstractoRunTimeException implements Templatable {

    private String emoteKey;

    public EmoteNotDefinedException(String key) {
        super("");
        this.emoteKey = key;
    }

    @Override
    public String getTemplateName() {
        return "emote_not_defined_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("emoteKey", this.emoteKey);
        return param;
    }
}
