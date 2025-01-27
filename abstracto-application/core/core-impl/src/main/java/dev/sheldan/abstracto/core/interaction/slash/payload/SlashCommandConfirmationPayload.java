package dev.sheldan.abstracto.core.interaction.slash.payload;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SlashCommandConfirmationPayload implements ButtonPayload {

    private CommandConfirmationAction action;
    private Long interactionId;

    public enum CommandConfirmationAction {
        CONFIRM, ABORT
    }
}
