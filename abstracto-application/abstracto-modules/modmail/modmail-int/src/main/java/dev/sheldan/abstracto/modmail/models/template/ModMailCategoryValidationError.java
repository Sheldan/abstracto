package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.ValidationError;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class ModMailCategoryValidationError implements ValidationError {
    private Long currentCategoryId;

    @Override
    public String getTemplateName() {
        return "modmail_category_not_setup";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> params = new HashMap<>();
        params.put("categoryId", currentCategoryId);
        return params;
    }
}
