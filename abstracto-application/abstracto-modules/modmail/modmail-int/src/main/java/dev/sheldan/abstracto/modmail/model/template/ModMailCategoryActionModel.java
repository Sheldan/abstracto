package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.utils.ChannelUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
/**
 * This model is used when confirming the setup up the mod mail configuration for the category in which the channels should be created
 * This model contains the actual JDA category object where the channels will be created in, and the id of said
 * category
 */
@Getter
@Setter
@Builder
public class ModMailCategoryActionModel {
    private Long serverId;
    private Long categoryId;

    public String getCategoryAsMention() {
        return ChannelUtils.getAsMention(this.getCategoryId());
    }
}
