package dev.sheldan.abstracto.modmail.setup;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.modmail.model.template.ModMailCategoryActionModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Category;

/**
 * This represents both an instance of a {@link DelayedActionConfig} used to be executed in the
 * {@link dev.sheldan.abstracto.core.service.DelayedActionService} and, as all {@link DelayedActionConfig}, as a
 * model when all setup steps are presented, and the member executing the setup command needs to confirm the changes.
 * This model is responsible to contain the values needed to displayed the mod mail category changes.
 */
@Getter
@Setter
@Builder
public class ModMailCategoryDelayedActionConfig implements DelayedActionConfig {
    private Long serverId;
    private Long categoryId;
    private Category category;

    @Override
    public String getTemplateName() {
        return "feature_setup_modmail_category_action";
    }

    @Override
    public Object getTemplateModel() {
        return ModMailCategoryActionModel
                .builder()
                .category(this.category)
                .categoryId(categoryId)
                .build();
    }
}
