package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class PostTargetStepException extends AbstractoRunTimeException implements Templatable {
    public PostTargetStepException(Throwable cause) {
        super("", cause);
    }

    @Override
    public String getTemplateName() {
        return "setup_post_target_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        if(getCause() instanceof Templatable) {
            Templatable templatable = (Templatable) getCause();
            stringStringHashMap.put("templateKey", templatable.getTemplateName());
            stringStringHashMap.put("templateModel", templatable.getTemplateModel());
        }
        stringStringHashMap.put("message", getCause().getMessage());

        return stringStringHashMap;
    }
}
