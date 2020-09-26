package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.ValidationErrorModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * This model is used when the category for creating mod mail threads is not properly setup
 * and when the feature is enabled via command. This will be rendered as a simple additional line of validation errors
 * after the command finished
 */
@Getter
@Setter
@Builder
public class ModMailCategoryValidationErrorModel implements ValidationErrorModel {
    private Long currentCategoryId;

    @Override
    public String getTemplateName() {
        return "setup_modmail_category_not_setup";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
