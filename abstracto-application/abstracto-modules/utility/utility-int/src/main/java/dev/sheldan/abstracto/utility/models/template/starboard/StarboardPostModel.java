package dev.sheldan.abstracto.utility.models.template.starboard;

import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.ServerContext;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
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
    private AUser user;
    private AChannel aChannel;
    private CachedMessage message;
    private Integer starCount;
    private String starLevelEmote;
}
