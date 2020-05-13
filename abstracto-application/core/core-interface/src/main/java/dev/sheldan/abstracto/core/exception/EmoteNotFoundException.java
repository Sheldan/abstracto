package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class EmoteNotFoundException extends AbstractoRunTimeException implements Templatable {

    private String emoteKey;
    private List<String> available;

    public EmoteNotFoundException(String key, List<String> availableEmotes) {
        super("");
        this.emoteKey = key;
        this.available = availableEmotes;
    }

    @Override
    public String getTemplateName() {
        return "emote_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("emoteKey", this.emoteKey);
        param.put("available", String.join(",", this.available));
        return param;
    }
}
