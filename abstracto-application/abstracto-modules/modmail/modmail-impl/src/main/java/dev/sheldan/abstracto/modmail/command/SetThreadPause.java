package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadClosedException;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SetThreadPause extends AbstractConditionableCommand {

    private static final String SET_THREAD_PAUSE_COMMAND = "setThreadPause";
    private static final String SET_THREAD_PAUSE_RESPONSE = "setThreadPause_response";
    private static final String PAUSED_PARAMETER = "paused";

    @Autowired
    private ModMailContextCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(event.getChannel().getIdLong());
        if(ModMailThreadState.CLOSED.equals(modMailThread.getState()) || ModMailThreadState.CLOSING.equals(modMailThread.getState())) {
            throw new ModMailThreadClosedException();
        }
        Boolean paused = slashCommandParameterService.getCommandOption(PAUSED_PARAMETER, event, Boolean.class);
        modMailThreadService.setPauseOfThreadTo(modMailThread, paused);
        return interactionService.replyEmbed(SET_THREAD_PAUSE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter newStateParameter = Parameter
            .builder()
            .name(PAUSED_PARAMETER)
            .type(Boolean.class)
            .templated(true)
            .build();

        List<Parameter> parameters = Arrays.asList(newStateParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModMailSlashCommandNames.MODMAIL)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName(SET_THREAD_PAUSE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SET_THREAD_PAUSE_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(ModMailModuleDefinition.MODMAIL)
                .help(helpInfo)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .templated(true)
                .parameters(parameters)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(requiresModMailCondition);
        return conditions;
    }
}
