package dev.sheldan.abstracto.core.interaction.slash;

import dev.sheldan.abstracto.core.command.Command;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Getter
@Builder
public class DriedSlashCommand {
    private Command command;
    private SlashCommandInteractionEvent event;
}
