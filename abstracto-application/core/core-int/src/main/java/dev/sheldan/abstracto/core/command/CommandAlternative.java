package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

public interface CommandAlternative extends Prioritized {
    boolean shouldExecute(UnParsedCommandParameter parameter, Guild guild);
    void execute(UnParsedCommandParameter parameter, Message message);
}
