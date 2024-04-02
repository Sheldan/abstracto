package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class UserCommandConfig {
    @Builder.Default
    private Set<CommandContext> contexts = new HashSet<>(List.of(CommandContext.GUILD));

    public static UserCommandConfig guildOnly() {
        return UserCommandConfig
                .builder()
                .contexts(Set.of(CommandContext.GUILD))
                .build();
    }

    public static UserCommandConfig all() {
        return UserCommandConfig
                .builder()
                .contexts(Set.of(CommandContext.ALL))
                .build();
    }

    public enum CommandContext {
        BOT_DM, DM, GUILD, ALL
    }

}

