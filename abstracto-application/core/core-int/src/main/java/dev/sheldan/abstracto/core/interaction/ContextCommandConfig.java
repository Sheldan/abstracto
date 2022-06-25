package dev.sheldan.abstracto.core.interaction;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.Command;

@Builder
@Getter
public class ContextCommandConfig {
    private Command.Type type;
    private String name;
    private MessageContextConfig messageContextConfig;
}
