package dev.sheldan.abstracto.core.models.cache;

import dev.sheldan.abstracto.core.models.dto.EmoteDto;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CachedReaction {
    private EmoteDto emote;
    private List<UserDto> users;
}
