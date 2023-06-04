package dev.sheldan.abstracto.suggestion.model.payload;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PollAddOptionButtonPayload implements ButtonPayload {
    private Long pollId;
    private Long serverId;
}
