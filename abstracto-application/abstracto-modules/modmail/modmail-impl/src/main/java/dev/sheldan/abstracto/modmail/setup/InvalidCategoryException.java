package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * This exception is thrown when the provided category used for creating mod mail thread is not valid.
 * (If it does not exist in the guild)
 */
public class InvalidCategoryException extends AbstractoRunTimeException implements Templatable {
    public InvalidCategoryException() {
        super("Invalid category given for setup");
    }

    @Override
    public String getTemplateName() {
        return "feature_setup_category_not_valid_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
