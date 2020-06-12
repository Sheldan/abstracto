package dev.sheldan.abstracto.modmail.models.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Category;

/**
 * Model which is used when setting up the mod mail feature. The category property will be used when there is already a category
 * defined to be used for mod mail threads and it is still a valid category.
 */
@Getter
@Setter
@Builder
public class SetupModMailCategoryMessageModel {
    private Category category;
}
