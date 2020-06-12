package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.service.ModMailRoleService;
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

    @Override
    public CommandResult execute(CommandContext commandContext) {
        ARole role = (ARole) commandContext.getParameters().getParameters().get(0);
        modMailRoleService.addRoleToModMailRoles(role);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter categoryId = Parameter.builder().name("role").type(ARole.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(categoryId);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("modMailRole");
        return CommandConfiguration.builder()
                .name("setModMailRole")
                .module(ModMailModuleInterface.MODMAIL)
                .aliases(aliases)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }
}
