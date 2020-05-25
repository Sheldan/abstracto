package dev.sheldan.abstracto.modmail.models.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Category;

@Getter
@Setter
@Builder
public class SetupModMailCategoryMessageModel {
    private Category category;
}
