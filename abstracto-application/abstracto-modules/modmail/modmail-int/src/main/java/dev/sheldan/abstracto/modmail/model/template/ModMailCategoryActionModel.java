package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Category;

/**
 * This model is used when confirming the setup up the mod mail configuration for the category in which the channels should be created
 * This model contains the actual JDA category object where the channels will be created in, and the id of said
 * category
 */
@Getter
@Setter
@Builder
public class ModMailCategoryActionModel {
    private Category category;
    private Long categoryId;
}
