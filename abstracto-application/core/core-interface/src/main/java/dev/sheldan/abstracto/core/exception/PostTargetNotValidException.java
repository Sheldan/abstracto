package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class PostTargetNotValidException extends AbstractoRunTimeException implements Templatable {

    private final String postTargetKey;
    private final List<String> availableTargets;

    public PostTargetNotValidException(String key, List<String> available) {
        super("");
        this.postTargetKey = key;
        this.availableTargets = available;
    }

    @Override
    public String getTemplateName() {
        return "post_target_not_valid";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("key", this.postTargetKey);
        param.put("available", String.join(",", this.availableTargets));
        return param;
    }
}
