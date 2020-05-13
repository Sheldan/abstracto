package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class PostTargetNotValidException extends AbstractoRunTimeException implements Templatable {

    private String postTargetKey;

    public PostTargetNotValidException(String key) {
        super("");
        this.postTargetKey = key;
    }

    @Override
    public String getTemplateName() {
        return "post_target_not_valid";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("key", this.postTargetKey);
        return param;
    }
}
