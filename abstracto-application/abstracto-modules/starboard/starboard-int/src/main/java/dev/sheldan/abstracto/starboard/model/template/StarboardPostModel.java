package dev.sheldan.abstracto.starboard.model.template;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@SuperBuilder
public class StarboardPostModel extends ServerContext {
    private User author;
    private TextChannel channel;
    private Long sourceChannelId;
    private CachedMessage message;
    private Integer starCount;
    private String starLevelEmote;
}
