package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.utils.ChannelUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Model which is used when setting up the mod mail feature. The category property will be used when there is already a category
 * defined to be used for mod mail threads and it is still a valid category.
 */
@Getter
@Setter
@Builder
public class SetupModMailCategoryMessageModel {
    private Long serverId;
    private Long categoryId;

    public String getCategoryAsMention() {
        return ChannelUtils.getAsMention(this.getCategoryId());
    }
}
