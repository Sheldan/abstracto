package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.utils.ChannelUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostTargetActionModel {
    private String postTargetKey;
    private Long channelId;

    public String getChannelAsMention() {
        return ChannelUtils.getAsMention(this.channelId);
    }
}
