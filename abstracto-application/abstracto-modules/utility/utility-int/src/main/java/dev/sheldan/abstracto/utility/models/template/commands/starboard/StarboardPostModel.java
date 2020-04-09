package dev.sheldan.abstracto.utility.models.template.commands.starboard;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@SuperBuilder
public class StarboardPostModel extends ServerContext {
    private Member author;
    private TextChannel channel;
    private UserDto user;
    private ChannelDto aChannel;
    private CachedMessage message;
    private Integer starCount;
    private String starLevelEmote;
}
