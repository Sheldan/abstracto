package dev.sheldan.abstracto.core.commands.config.cooldown;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RemoveCommandMemberCooldown extends AbstractConditionableCommand {

    private static final String REMOVE_COMMAND_MEMBER_COOLDOWN_COMMAND = "removeCommandMemberCooldown";
    private static final String COMMAND_NAME_PARAMETER = "commandName";
    private static final String REMOVE_COMMAND_MEMBER_COOLDOWN_RESPONSE_TEMPLATE_KEY = "removeCommandMemberCooldown_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String commandName = slashCommandParameterService.getCommandOption(COMMAND_NAME_PARAMETER, event, String.class);
        commandService.setServerCooldownTo(commandName, event.getGuild(), null);
        return interactionService.replyEmbed(REMOVE_COMMAND_MEMBER_COOLDOWN_RESPONSE_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter commandName = Parameter
                .builder()
                .name(COMMAND_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(commandName);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CoreSlashCommandNames.COOLDOWN)
                .groupName("commandMember")
                .commandName("remove")
                .build();

        return CommandConfiguration.builder()
                .name(REMOVE_COMMAND_MEMBER_COOLDOWN_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
