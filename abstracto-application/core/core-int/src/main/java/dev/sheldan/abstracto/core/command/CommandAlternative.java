package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import net.dv8tion.jda.api.entities.Message;

public interface CommandAlternative extends Prioritized {
    boolean matches(UnParsedCommandParameter parameter);
    void execute(UnParsedCommandParameter parameter, Message message);
}
