package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class CategoryNotFoundException extends AbstractoRunTimeException implements Templatable {

    private Long categoryId;
    private Long guildId;

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(Long categoryId, Long guildId) {
        super("");
        this.categoryId = categoryId;
        this.guildId = guildId;
    }

    @Override
    public String getTemplateName() {
        return "category_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> param = new HashMap<>();
        param.put("categoryId", this.categoryId);
        param.put("guildId", this.guildId);
        return param;
    }
}
