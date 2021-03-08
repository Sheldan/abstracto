package dev.sheldan.abstracto.core.models.cache;

import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CachedReaction {
    private CachedEmote emote;
    private Boolean self;
    private ServerUser user;
}
