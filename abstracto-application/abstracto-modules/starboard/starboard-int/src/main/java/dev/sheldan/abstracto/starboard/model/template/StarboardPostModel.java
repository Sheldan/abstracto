package dev.sheldan.abstracto.starboard.model.template;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

@Getter
@Setter
@SuperBuilder
public class StarboardPostModel extends ServerContext {
    private UserDisplay authorUser;
    private MemberDisplay authorMember;
    private GuildMessageChannel channel;
    private Long sourceChannelId;
    private CachedMessage message;
    private Integer starCount;
    private String starLevelEmote;
}
