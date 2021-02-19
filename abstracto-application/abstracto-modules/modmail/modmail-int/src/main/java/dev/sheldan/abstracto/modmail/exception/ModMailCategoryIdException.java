package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.modmail.models.exception.ModMailCategoryIdExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;
import lombok.Getter;
import lombok.Setter;

/**
 * This exception is thrown when a {@link net.dv8tion.jda.api.entities.Member} tries to set the mod mail category
 * via a command, and the new value does not qualify as a valid {@link net.dv8tion.jda.api.entities.Category}
 * in the member executes the command in
 */
@Getter
@Setter
public class ModMailCategoryIdException extends AbstractoRunTimeException implements Templatable {
    private final ModMailCategoryIdExceptionModel model;

    public ModMailCategoryIdException(Long categoryId) {
        super("Mod mail category has not been setup");
        this.model = ModMailCategoryIdExceptionModel.builder().categoryId(categoryId).build();
    }

    @Override
    public String getTemplateName() {
        return "modmail_category_not_setup_exception";
    }

    @Override
    public Object getTemplateModel() {
       return model;
    }
}
