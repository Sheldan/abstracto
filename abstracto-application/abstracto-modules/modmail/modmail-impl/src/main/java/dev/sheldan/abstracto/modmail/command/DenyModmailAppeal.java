package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailMode;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadClosedException;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class DenyModmailAppeal extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "denyappeal";
    private static final String FULL_COMMAND_NAME = "denyModmailAppeal";
    private static final String REASON_PARAMETER = "reason";

    private static final String RESPONSE_TEMPLATE = "denyModmailAppeal_response";

    @Autowired
    private ModMailContextCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(event.getChannel().getIdLong());
        if(ModMailThreadState.CLOSED.equals(modMailThread.getState()) || ModMailThreadState.CLOSING.equals(modMailThread.getState())) {
            throw new ModMailThreadClosedException();
        }
        String reason = slashCommandParameterService.getCommandOption(REASON_PARAMETER, event, String.class);
        CompletableFuture<InteractionHook> response = interactionService.replyEmbed(RESPONSE_TEMPLATE, event);
        CompletableFuture<Void> threadFuture = modMailThreadService.rejectAppeal(modMailThread, reason, event.getMember());
        return CompletableFuture.allOf(response, threadFuture)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter responseText = Parameter
                .builder()
                .name(REASON_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(responseText);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModMailSlashCommandNames.MODMAIL)
                .commandName(COMMAND_NAME)
                .build();

        return CommandConfiguration.builder()
                .name(FULL_COMMAND_NAME)
                .module(ModMailModuleDefinition.MODMAIL)
                .parameters(parameters)
                .slashCommandConfig(slashCommandConfig)
                .help(helpInfo)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .templated(true)
                .build();
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(requiresModMailCondition);
        return conditions;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return List.of(ModMailMode.MOD_MAIL_APPEALS);
    }
}
