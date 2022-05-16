package dev.sheldan.abstracto.core.models.template.commands.help;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.CommandCoolDownConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

@Getter
@Setter
@Builder
public class HelpCommandDetailsModel {
    private CommandConfiguration command;
    private List<String> serverSpecificAliases;
    private String usage;
    private List<Role> allowedRoles;
    private Boolean restricted;
    private CommandCoolDownConfig cooldowns;
    private List<String> effects;
}
