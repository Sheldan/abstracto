package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.service.QuickReplyService;
import dev.sheldan.abstracto.modmail.service.management.QuickReplyManagementService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuickReplyDelete extends AbstractConditionableCommand {
    private static final String DELETE_QUICK_REPLY_COMMAND = "deleteQuickReply";
    private static final String DELETE_QUICK_REPLY_RESPONSE_TEMPLATE_KEY = "deleteQuickReply_response";
    private static final String QUICK_REPLY_NAME_PARAMETER = "name";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private QuickReplyService customCommandService;

    @Autowired
    private QuickReplyManagementService quickReplyManagementService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SlashCommandService slashCommandService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(QUICK_REPLY_NAME_PARAMETER, event, String.class);
        customCommandService.deleteQuickReply(name, event.getGuild());
        return slashCommandService.completeConfirmableCommand(event, DELETE_QUICK_REPLY_RESPONSE_TEMPLATE_KEY)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), QUICK_REPLY_NAME_PARAMETER)) {
            String input = event.getFocusedOption().getValue();
            AServer server = serverManagementService.loadServer(event.getGuild());
            return quickReplyManagementService.getQuickRepliesContaining(input, server)
                .stream().map(quickReply -> quickReply.getName().toLowerCase())
                .toList();
        }
        return new ArrayList<>();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter quickReplyNameParameter = Parameter
            .builder()
            .name(QUICK_REPLY_NAME_PARAMETER)
            .templated(true)
            .supportsAutoComplete(true)
            .type(String.class)
            .build();

        List<Parameter> parameters = Arrays.asList(quickReplyNameParameter);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .rootCommandName(ModMailSlashCommandNames.MODMAIL)
            .commandName("deleteQuickReply")
            .build();

        return CommandConfiguration.builder()
            .name(DELETE_QUICK_REPLY_COMMAND)
            .module(UtilityModuleDefinition.UTILITY)
            .templated(true)
            .async(true)
            .slashCommandConfig(slashCommandConfig)
            .causesReaction(true)
            .slashCommandOnly(true)
            .requiresConfirmation(true)
            .supportsEmbedException(true)
            .parameters(parameters)
            .help(helpInfo)
            .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }
}
