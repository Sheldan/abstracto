package dev.sheldan.abstracto.core.models.cache;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CachedReactions {
    private CachedEmote emote;
    private Boolean self;
    private List<ServerUser> users;

    public CachedReaction getReactionForSpecificUser(ServerUser serverUser) {
        ServerUser matchingUser = users.stream().filter(serverUser1 -> serverUser1.equals(serverUser)).findAny().orElseThrow(() -> new AbstractoRunTimeException("Server user not found."));
        return CachedReaction.builder().self(self).emote(emote).user(matchingUser).build();
    }
}
