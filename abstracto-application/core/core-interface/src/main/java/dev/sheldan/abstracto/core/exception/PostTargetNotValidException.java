package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.PostTargetNotValidExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.List;

public class PostTargetNotValidException extends AbstractoRunTimeException implements Templatable {

    private final PostTargetNotValidExceptionModel model;

    public PostTargetNotValidException(String key, List<String> available) {
        super("Given post target was not in the list of valid post targets");
        this.model = PostTargetNotValidExceptionModel.builder().availableTargets(available).postTargetKey(key).build();
    }

    @Override
    public String getTemplateName() {
        return "post_target_not_valid_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
