package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.PostTargetNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class PostTargetNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final PostTargetNotFoundExceptionModel model;

    public PostTargetNotFoundException(String key) {
        super("Post target not found");
        this.model = PostTargetNotFoundExceptionModel.builder().postTargetKey(key).build();
    }

    @Override
    public String getTemplateName() {
        return "post_target_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
       return model;
    }
}
