package dev.sheldan.abstracto.core.models.listener;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class GuildMessageEmbedEventModel {
    @Builder.Default
    private List<MessageEmbed> embeds = new ArrayList<>();
    private Long messageId;
    private Long channelId;
    private Long serverId;
}
