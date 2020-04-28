package dev.sheldan.abstracto.core.models.template.commands.help;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class HelpCommandDetailsModel extends UserInitiatedServerContext {
    private CommandConfiguration command;
}
