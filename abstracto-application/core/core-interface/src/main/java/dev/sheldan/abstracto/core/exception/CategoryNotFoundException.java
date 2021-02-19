package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.CategoryNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class CategoryNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final CategoryNotFoundExceptionModel model;

    public CategoryNotFoundException(Long categoryId, Long guildId) {
        super("Category not found");
        this.model = CategoryNotFoundExceptionModel.builder().categoryId(categoryId).guildId(guildId).build();
    }

    @Override
    public String getTemplateName() {
        return "category_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
