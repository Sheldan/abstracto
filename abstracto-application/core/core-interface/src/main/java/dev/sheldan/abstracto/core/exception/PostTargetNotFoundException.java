package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class PostTargetNotFoundException extends AbstractoRunTimeException implements Templatable {

    private String postTargetKey;

    public PostTargetNotFoundException(String key) {
        super("");
        this.postTargetKey = key;
    }

    @Override
    public String getTemplateName() {
        return "post_target_not_found";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("key", this.postTargetKey);
        return param;
    }
}
