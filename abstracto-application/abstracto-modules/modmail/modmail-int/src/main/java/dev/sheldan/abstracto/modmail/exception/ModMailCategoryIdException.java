package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

/**
 * This exception is thrown when a {@link net.dv8tion.jda.api.entities.Member} tries to set the mod mail category
 * via a command, and the new value does not qualify as a valid {@link net.dv8tion.jda.api.entities.Category}
 * in the member executes the command in
 */
@Getter
@Setter
public class ModMailCategoryIdException extends AbstractoRunTimeException implements Templatable {
    private Long categoryId;

    public ModMailCategoryIdException(Long categoryId) {
        super("");
        this.categoryId = categoryId;
    }

    @Override
    public String getTemplateName() {
        return "modmail_category_not_setup";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Long> params = new HashMap<>();
        params.put("categoryId", this.categoryId);
        return params;
    }
}
