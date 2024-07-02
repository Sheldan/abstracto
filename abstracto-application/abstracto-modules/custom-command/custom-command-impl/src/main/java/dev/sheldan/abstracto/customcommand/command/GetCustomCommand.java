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
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.customcommand.config.CustomCommandFeatureDefinition;
import dev.sheldan.abstracto.customcommand.config.CustomCommandSlashCommandNames;
import dev.sheldan.abstracto.customcommand.model.command.CustomCommandResponseModel;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandService;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class GetCustomCommand extends AbstractConditionableCommand {

    private static final String GET_CUSTOM_COMMAND_COMMAND = "getCustomCommand";
    private static final String CUSTOM_COMMAND_NAME_PARAMETER = "commandName";
    private static final String GET_CUSTOM_COMMAND_RESPONSE_TEMPLATE_KEY = "getCustomCommand_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private CustomCommandService customCommandService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(CUSTOM_COMMAND_NAME_PARAMETER, event, String.class);
        CustomCommand customCommand;
        if(ContextUtils.isUserCommand(event)) {
            customCommand = customCommandService.getUserCustomCommand(name, event.getUser());
        } else {
            customCommand = customCommandService.getCustomCommand(name, event.getGuild());
        }
        CustomCommandResponseModel model = CustomCommandResponseModel
                .builder()
                .additionalText(customCommand.getAdditionalMessage())
                .build();
        return interactionService.replyEmbed(GET_CUSTOM_COMMAND_RESPONSE_TEMPLATE_KEY, model, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), CUSTOM_COMMAND_NAME_PARAMETER)) {
            String input = event.getFocusedOption().getValue();
            if(ContextUtils.isNotUserCommand(event)) {
                return customCommandService.getCustomCommandsStartingWith(input, event.getGuild())
                        .stream()
                        .map(CustomCommand::getName)
                        .toList();
            } else {
                return customCommandService.getUserCustomCommandsStartingWith(input, event.getUser())
                        .stream()
                        .map(CustomCommand::getName)
                        .toList();
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return CustomCommandFeatureDefinition.CUSTOM_COMMAND;
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
                .rootCommandName(CustomCommandSlashCommandNames.CUSTOM_COMMAND_PUBLIC)
                .userRootCommandName(CustomCommandSlashCommandNames.CUSTOM_COMMAND)
                .commandName("get")
                .build();

        return CommandConfiguration.builder()
                .name(GET_CUSTOM_COMMAND_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
