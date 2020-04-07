package dev.sheldan.abstracto.core.command.execution;

import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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
