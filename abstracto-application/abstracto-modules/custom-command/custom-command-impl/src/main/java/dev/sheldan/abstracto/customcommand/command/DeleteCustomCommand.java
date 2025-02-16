package dev.sheldan.abstracto.customcommand.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.customcommand.config.CustomCommandFeatureDefinition;
import dev.sheldan.abstracto.customcommand.config.CustomCommandSlashCommandNames;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandManagementService;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandService;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class DeleteCustomCommand extends AbstractConditionableCommand {

    private static final String DELETE_CUSTOM_COMMAND_COMMAND = "deleteCustomCommand";
    private static final String DELETE_CUSTOM_COMMAND_RESPONSE_TEMPLATE_KEY = "deleteCustomCommand_response";
    private static final String CUSTOM_COMMAND_NAME_PARAMETER = "commandName";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private CustomCommandService customCommandService;

    @Autowired
    private CustomCommandManagementService customCommandManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(CUSTOM_COMMAND_NAME_PARAMETER, event, String.class);
        if(ContextUtils.isUserCommand(event)) {
            customCommandService.deleteUserCustomCommand(name, event.getUser());
        } else {
            customCommandService.deleteCustomCommand(name, event.getGuild());
        }
        return interactionService.replyEmbed(DELETE_CUSTOM_COMMAND_RESPONSE_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), CUSTOM_COMMAND_NAME_PARAMETER)) {
            String input = event.getFocusedOption().getValue();
            if(ContextUtils.isUserCommand(event)) {
                AUser user = userManagementService.loadUser(event.getUser().getIdLong());
                return customCommandManagementService.getUserCustomCommandsContaining(input, user)
                    .stream().map(customCommand -> customCommand.getName().toLowerCase())
                    .toList();
            } else {
                AServer server = serverManagementService.loadServer(event.getGuild());
                return customCommandManagementService.getCustomCommandsContaining(input, server)
                    .stream().map(customCommand -> customCommand.getName().toLowerCase())
                    .toList();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter commandNameParameter = Parameter
                .builder()
                .name(CUSTOM_COMMAND_NAME_PARAMETER)
                .templated(true)
                .supportsAutoComplete(true)
                .type(String.class)
                .build();

        List<Parameter> parameters = Arrays.asList(commandNameParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CustomCommandSlashCommandNames.CUSTOM_COMMAND)
                .commandName("delete")
                .build();

        return CommandConfiguration.builder()
                .name(DELETE_CUSTOM_COMMAND_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CustomCommandFeatureDefinition.CUSTOM_COMMAND;
    }
}
