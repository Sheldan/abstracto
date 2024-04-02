package dev.sheldan.abstracto.customcommand.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.customcommand.config.CustomCommandFeatureDefinition;
import dev.sheldan.abstracto.customcommand.config.CustomCommandSlashCommandNames;
import dev.sheldan.abstracto.customcommand.model.command.ListCustomCommandsResponseModel;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ListCustomCommands extends AbstractConditionableCommand {

    private static final String CREATE_CUSTOM_COMMAND_COMMAND = "listCustomCommands";
    private static final String LIST_CUSTOM_COMMANDS_TEMPLATE_KEY = "listCustomCommands_response";
    private static final String NO_CUSTOM_COMMANDS_TEMPLATE_KEY = "listCustomCommands_no_commands_response";

    @Autowired
    private CustomCommandService customCommandService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private PaginatorService paginatorService;


    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        List<CustomCommand> customCommands;
        if(ContextUtils.isUserCommand(event)) {
            customCommands = customCommandService.getUserCustomCommands(event.getUser());
        } else {
            customCommands = customCommandService.getCustomCommands(event.getGuild());
        }
        if(customCommands.isEmpty()) {
            return interactionService.replyEmbed(NO_CUSTOM_COMMANDS_TEMPLATE_KEY, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        }
        ListCustomCommandsResponseModel model = ListCustomCommandsResponseModel.fromCommands(customCommands);
        return paginatorService.createPaginatorFromTemplate(LIST_CUSTOM_COMMANDS_TEMPLATE_KEY, model, event)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public FeatureDefinition getFeature() {
        return CustomCommandFeatureDefinition.CUSTOM_COMMAND;
    }

    @Override
    public CommandConfiguration getConfiguration() {
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
                .commandName("list")
                .build();

        return CommandConfiguration.builder()
                .name(CREATE_CUSTOM_COMMAND_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .build();
    }
}
