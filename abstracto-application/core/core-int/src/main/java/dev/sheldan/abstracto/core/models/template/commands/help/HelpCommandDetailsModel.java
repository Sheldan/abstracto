package dev.sheldan.abstracto.core.models.template.commands.help;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.CommandCoolDownConfig;
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
    private List<String> serverSpecificAliases;
    private String usage;
    private List<Role> allowedRoles;
    private Boolean restricted;
    private CommandCoolDownConfig cooldowns;
    private List<String> effects;
}
