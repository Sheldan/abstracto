package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.modmail.models.template.ModMailCategoryActionModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Category;

@Getter
@Setter
@Builder
public class ModMailCategoryDelayedActionConfig implements DelayedActionConfig {
    private Long serverId;
    private AConfig value;
    private Category category;

    @Override
    public String getTemplateName() {
        return "setup_modmail_category_action";
    }

    @Override
    public Object getTemplateModel() {
        return ModMailCategoryActionModel
                .builder()
                .category(this.category)
                .categoryId(value.getLongValue())
                .build();
    }
}
