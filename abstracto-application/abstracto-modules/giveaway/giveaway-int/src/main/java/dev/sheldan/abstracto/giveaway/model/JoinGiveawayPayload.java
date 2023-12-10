package dev.sheldan.abstracto.giveaway.model;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinGiveawayPayload implements ButtonPayload {
    private Long giveawayId;
    private Long serverId;
}
