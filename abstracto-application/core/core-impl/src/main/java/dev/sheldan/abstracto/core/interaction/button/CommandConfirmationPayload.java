package dev.sheldan.abstracto.core.interaction.button;

import dev.sheldan.abstracto.core.command.execution.DriedCommandContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class CommandConfirmationPayload implements ButtonPayload {
    private DriedCommandContext commandContext;
    private CommandConfirmationAction action;
    private String otherButtonComponentId;

    public enum CommandConfirmationAction {
        CONFIRM, ABORT
    }
}

