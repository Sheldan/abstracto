package dev.sheldan.abstracto.customcommand.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.customcommand.config.CustomCommandFeatureDefinition;
import dev.sheldan.abstracto.customcommand.config.CustomCommandSlashCommandNames;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class CreateCustomCommand extends AbstractConditionableCommand {

    private static final String CREATE_CUSTOM_COMMAND_COMMAND = "createCustomCommand";
    private static final String CUSTOM_COMMAND_NAME_PARAMETER = "commandName";
    private static final String CUSTOM_COMMAND_CONTENT_PARAMETER = "response";
    private static final String CREATE_CUSTOM_COMMAND_RESPONSE_TEMPLATE_KEY = "createCustomCommand_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private CustomCommandService customCommandService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(CUSTOM_COMMAND_NAME_PARAMETER, event, String.class);
        String content = slashCommandParameterService.getCommandOption(CUSTOM_COMMAND_CONTENT_PARAMETER, event, String.class);

        customCommandService.createCustomCommand(name, content, event.getMember());
        return interactionService.replyEmbed(CREATE_CUSTOM_COMMAND_RESPONSE_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
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
                .type(String.class)
                .build();

        Parameter commandContentParameter = Parameter
                .builder()
                .name(CUSTOM_COMMAND_CONTENT_PARAMETER)
                .templated(true)
                .type(String.class)
                .build();

        List<Parameter> parameters = Arrays.asList(commandNameParameter, commandContentParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CustomCommandSlashCommandNames.CUSTOM_COMMAND)
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name(CREATE_CUSTOM_COMMAND_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
