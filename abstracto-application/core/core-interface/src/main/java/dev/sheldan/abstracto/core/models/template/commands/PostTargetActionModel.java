package dev.sheldan.abstracto.core.models.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@Builder
public class PostTargetActionModel {
    private String postTargetKey;
    private Long channelId;
    private TextChannel channel;
}
