package dev.sheldan.abstracto.suggestion.model.payload;

import dev.sheldan.abstracto.core.interaction.menu.SelectMenuPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServerPollSelectionMenuPayload implements SelectMenuPayload {
    private Long pollId;
    private Long serverId;
}
