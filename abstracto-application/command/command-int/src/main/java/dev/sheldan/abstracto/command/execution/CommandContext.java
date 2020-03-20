package dev.sheldan.abstracto.command.execution;

import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

@Builder
@Getter
public class CommandContext {
    private TextChannel channel;
    private Guild guild;
    private Member author;
    private Message message;
    private UserInitiatedServerContext userInitiatedContext;
    private Parameters parameters;
    private JDA jda;
}
