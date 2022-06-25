package dev.sheldan.abstracto.core.interaction.button;

import dev.sheldan.abstracto.core.command.execution.DriedCommandContext;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommandConfirmationModel {
    private String confirmButtonId;
    private String abortButtonId;
    private DriedCommandContext driedCommandContext;
    private String commandName;
}
