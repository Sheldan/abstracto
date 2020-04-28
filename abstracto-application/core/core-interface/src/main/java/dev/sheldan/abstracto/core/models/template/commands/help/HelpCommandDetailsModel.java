package dev.sheldan.abstracto.core.models.template.commands.help;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class HelpCommandDetailsModel extends UserInitiatedServerContext {
    private CommandConfiguration command;
    private List<Role> allowedRoles;
    private List<Role> immuneRoles;
    private Boolean restricted;
}
