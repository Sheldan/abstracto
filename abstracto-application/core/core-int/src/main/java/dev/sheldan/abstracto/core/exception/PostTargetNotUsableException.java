package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.PostTargetNotValidExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class PostTargetNotUsableException extends AbstractoRunTimeException implements Templatable {

    private final PostTargetNotValidExceptionModel model;

    public PostTargetNotUsableException(String key) {
        super("The post target is not setup or has been disabled.");
        this.model = PostTargetNotValidExceptionModel
                .builder()
                .postTargetKey(key)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "post_target_not_usable_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
