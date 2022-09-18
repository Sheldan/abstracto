package dev.sheldan.abstracto.core.command.execution;

import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.List;

@Builder
@Getter
@Setter
public class CommandContext {
    private GuildMessageChannel channel;
    private Guild guild;
    private Member author;
    private Message message;
    private UserInitiatedServerContext userInitiatedContext;
    private Parameters parameters;
    private JDA jda;
    private List<UndoActionInstance> undoActions;
}
