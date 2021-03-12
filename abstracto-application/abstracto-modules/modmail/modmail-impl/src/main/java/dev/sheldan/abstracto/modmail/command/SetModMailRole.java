package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.service.ModMailRoleService;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This command is used to add roles to the roles being pinged when a new mod mail thread is opened.
 * The method this command uses automatically adds the mentioned roles to the roles which are allowed to execute
 * the mod mail related commands.
 */
@Component
public class SetModMailRole extends AbstractConditionableCommand {

    @Autowired
    private ModMailRoleService modMailRoleService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Role jdaRole = (Role) commandContext.getParameters().getParameters().get(0);
        ARole role = roleManagementService.findRole(jdaRole.getIdLong());
        modMailRoleService.addRoleToModMailRoles(role);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter categoryId = Parameter.builder().name("role").type(Role.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(categoryId);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("modMailRole");
        return CommandConfiguration.builder()
                .name("setModMailRole")
                .module(ModMailModuleDefinition.MODMAIL)
                .aliases(aliases)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }
}
