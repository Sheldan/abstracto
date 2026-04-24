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
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadClosedException;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SnoozeThreadReminder extends AbstractConditionableCommand {

    private static final String SNOOZE_THREAD_REMINDER_COMMAND = "snoozeThreadReminder";
    private static final String SNOOZE_THREAD_REMINDER_RESPONSE = "snoozeThreadReminder_response";
    private static final String DURATION_PARAMETER = "duration";

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
        String durationString = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, Duration.class, String.class);
        Duration duration = ParseUtils.parseDuration(durationString);
        modMailThreadService.snoozeThreadReminder(modMailThread, duration);
        return interactionService.replyEmbed(SNOOZE_THREAD_REMINDER_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter durationParameter = Parameter
            .builder()
            .name(DURATION_PARAMETER)
            .type(Duration.class)
            .templated(true)
            .build();

        List<Parameter> parameters = Arrays.asList(durationParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModMailSlashCommandNames.MODMAIL)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName(SNOOZE_THREAD_REMINDER_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SNOOZE_THREAD_REMINDER_COMMAND)
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
