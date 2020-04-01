package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CachedReaction {
    private AEmote emote;
    private List<AUser> users;
}
