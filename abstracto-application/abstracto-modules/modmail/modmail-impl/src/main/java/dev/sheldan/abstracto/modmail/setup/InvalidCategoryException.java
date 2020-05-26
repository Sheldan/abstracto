package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

public class InvalidCategoryException extends AbstractoRunTimeException implements Templatable {
    public InvalidCategoryException() {
        super("");
    }

    @Override
    public String getTemplateName() {
        return "setup_category_not_valid_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
