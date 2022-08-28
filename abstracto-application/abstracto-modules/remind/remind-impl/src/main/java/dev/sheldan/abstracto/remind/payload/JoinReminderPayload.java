package dev.sheldan.abstracto.remind.payload;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinReminderPayload implements ButtonPayload {
    private Long reminderId;
    private Long serverId;
    private Long remindedUserId;
}
