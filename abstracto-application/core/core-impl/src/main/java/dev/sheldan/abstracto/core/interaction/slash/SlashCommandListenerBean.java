package dev.sheldan.abstracto.core.interaction.slash;

import static dev.sheldan.abstracto.core.command.CommandReceivedHandler.COMMAND_CONFIRMATION_MESSAGE_TEMPLATE_KEY;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.CommandConfirmationModel;
import dev.sheldan.abstracto.core.interaction.slash.payload.SlashCommandConfirmationPayload;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SlashCommandListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    @Getter
    private List<Command> commands = new ArrayList<>();

    @Autowired
    @Qualifier("slashCommandExecutor")
    private TaskExecutor slashCommandExecutor;

    @Autowired
    @Qualifier("slashCommandAutoCompleteExecutor")
    private TaskExecutor slashCommandAutoCompleteExecutor;

    @Autowired
    private SlashCommandListenerBean self;

    @Autowired
    private CommandService commandService;

    @Autowired
    private List<PostCommandExecution> executions;

    @Autowired
    private MetricService metricService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    private static final Map<Long, DriedSlashCommand> COMMANDS_WAITING_FOR_CONFIRMATION = new ConcurrentHashMap<>();
    public static final String SLASH_COMMAND_CONFIRMATION_ORIGIN = "SLASH_COMMAND_CONFIRMATION";

    public static final CounterMetric SLASH_COMMANDS_PROCESSED_COUNTER = CounterMetric
            .builder()
            .name(CommandReceivedHandler.COMMAND_PROCESSED)
            .tagList(Arrays.asList(MetricTag.getTag(CommandReceivedHandler.STATUS_TAG, "processed"), MetricTag.getTag(CommandReceivedHandler.TYPE_TAG, "slash")))
            .build();

    public List<Command> getSlashCommands() {
        if(commands == null || commands.isEmpty()) {
            return new ArrayList<>();
        }
        return commands.stream()
                .filter(command -> command.getConfiguration()
                        .getSlashCommandConfig().isEnabled())
                .collect(Collectors.toList());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if(commands == null || commands.isEmpty()) return;
            log.debug("Executing slash command in guild {} from user {}.", event.getGuild().getIdLong(), event.getMember().getIdLong());
            CompletableFuture.runAsync(() ->  self.executeListenerLogic(event), slashCommandExecutor).exceptionally(throwable -> {
                log.error("Failed to execute listener logic in async slash command event.", throwable);
                return null;
            });
        } catch (Exception exception) {
            log.error("Failed to process slash command interaction event.", exception);
        }
    }

    @Transactional
    public void executeListenerLogic(SlashCommandInteractionEvent event) {
        Optional<Command> potentialCommand = findCommand(event);
        potentialCommand.ifPresent(command -> {
            metricService.incrementCounter(SLASH_COMMANDS_PROCESSED_COUNTER);
            try {
                commandService.isCommandExecutable(command, event).thenAccept(conditionResult -> {
                    self.executeCommand(event, command, conditionResult);
                }).exceptionally(throwable -> {
                    log.error("Error while executing command {}", command.getConfiguration().getName(), throwable);
                    CommandResult commandResult = CommandResult.fromError(throwable.getMessage(), throwable);
                    self.executePostCommandListener(command, event, commandResult);
                    return null;
                });
            } catch (Exception exception) {
                log.error("Error while checking if command {} is executable.", command.getConfiguration().getName(), exception);
                CommandResult commandResult = CommandResult.fromError(exception.getMessage(), exception);
                self.executePostCommandListener(command, event, commandResult);
            }
        });
    }

    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        try {
            if(commands == null || commands.isEmpty()) return;
            CompletableFuture.runAsync(() ->  self.executeAutCompleteListenerLogic(event), slashCommandAutoCompleteExecutor).exceptionally(throwable -> {
                log.error("Failed to execute listener logic in async auto complete interaction event.", throwable);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to process slash command auto complete interaction.", e);
        }
    }

    @Transactional
    public void executeAutCompleteListenerLogic(CommandAutoCompleteInteractionEvent event) {
        Optional<Command> potentialCommand = findCommand(event);
        potentialCommand.ifPresent(command -> {
            try {
                List<String> fullRepliesList = command.performAutoComplete(event);
                List<String> replies = fullRepliesList.subList(0, Math.min(fullRepliesList.size(), OptionData.MAX_CHOICES));
                event.replyChoiceStrings(replies).queue(unused -> {},
                        throwable -> log.error("Failed to response to complete of command {} in guild {}.", command.getConfiguration().getName(), event.getGuild().getIdLong()));
            } catch (Exception exception) {
                log.error("Error while executing autocomplete of command {}.", command.getConfiguration().getName(), exception);
            }
        });
    }

    @Transactional
    public void continueSlashCommand(Long interactionId, ButtonInteractionEvent buttonInteractionEvent) {
        if(COMMANDS_WAITING_FOR_CONFIRMATION.containsKey(interactionId)) {
            DriedSlashCommand driedSlashCommand = COMMANDS_WAITING_FOR_CONFIRMATION.get(interactionId);
            Command commandInstance = driedSlashCommand.getCommand();
            String commandName = commandInstance.getConfiguration().getName();
            log.info("Continuing slash command {}", commandName);
            commandInstance.executeSlash(driedSlashCommand.getEvent()).thenApply(commandResult -> {
                log.info("Command {} in server {} was executed after confirmation.", commandName, buttonInteractionEvent.getGuild().getIdLong());
                return commandResult;
            }).thenAccept(commandResult -> {
                self.executePostCommandListener(commandInstance, driedSlashCommand.getEvent(), commandResult);
                COMMANDS_WAITING_FOR_CONFIRMATION.remove(interactionId);
            }).exceptionally(throwable -> {
                log.error("Error while handling post execution of command with confirmation {}", commandName, throwable);
                CommandResult commandResult = CommandResult.fromError(throwable.getMessage(), throwable);
                self.executePostCommandListener(commandInstance, driedSlashCommand.getEvent(), commandResult);
                COMMANDS_WAITING_FOR_CONFIRMATION.remove(interactionId);
                return null;
            });
        } else {
            log.warn("Interaction was not found in internal map - not continuing interaction from user {} in server {}.", buttonInteractionEvent.getUser().getIdLong(), buttonInteractionEvent.getGuild().getIdLong());
        }
    }

    @Transactional
    public void removeSlashCommandConfirmationInteraction(Long interactionId, String confirmationPayload, String abortPayload) {
        if(COMMANDS_WAITING_FOR_CONFIRMATION.containsKey(interactionId)) {
            DriedSlashCommand removedSlashCommand = COMMANDS_WAITING_FOR_CONFIRMATION.remove(interactionId);
            SlashCommandInteractionEvent event = removedSlashCommand.getEvent();
            event.getInteraction().getHook().deleteOriginal().queue();
            log.info("Remove interaction for command {} in server {} from user {}.", removedSlashCommand.getCommand().getConfiguration().getName(), event.getGuild().getIdLong(), event.getUser().getIdLong());
        } else {
            log.info("Did not find interaction to clean up.");
        }
        componentPayloadManagementService.deletePayloads(Arrays.asList(confirmationPayload, abortPayload));
    }

    @Transactional(rollbackFor = AbstractoRunTimeException.class)
    public void executeCommand(SlashCommandInteractionEvent event, Command command, ConditionResult conditionResult) {
        String commandName = command.getConfiguration().getName();
        if(command.getConfiguration().isRequiresConfirmation() && conditionResult.isResult()) {
            DriedSlashCommand slashCommand = DriedSlashCommand
                .builder()
                .command(command)
                .event(event)
                .build();
            COMMANDS_WAITING_FOR_CONFIRMATION.put(event.getIdLong(), slashCommand);
            String confirmationId = componentService.generateComponentId();
            String abortId = componentService.generateComponentId();
            SlashCommandConfirmationPayload confirmPayload = SlashCommandConfirmationPayload
                .builder()
                .action(SlashCommandConfirmationPayload.CommandConfirmationAction.CONFIRM)
                .interactionId(event.getIdLong())
                .build();
            Long serverId = event.getGuild().getIdLong();
            AServer server = serverManagementService.loadServer(event.getGuild());
            componentPayloadService.createButtonPayload(confirmationId, confirmPayload, SLASH_COMMAND_CONFIRMATION_ORIGIN, server);
            SlashCommandConfirmationPayload denialPayload = SlashCommandConfirmationPayload
                .builder()
                .action(SlashCommandConfirmationPayload.CommandConfirmationAction.ABORT)
                .interactionId(event.getIdLong())
                .build();
            componentPayloadService.createButtonPayload(abortId, denialPayload, SLASH_COMMAND_CONFIRMATION_ORIGIN, server);
            CommandConfirmationModel model = CommandConfirmationModel
                .builder()
                .abortButtonId(abortId)
                .confirmButtonId(confirmationId)
                .commandName(commandName)
                .build();
            Long userId = event.getUser().getIdLong();
            interactionService.replyEmbed(COMMAND_CONFIRMATION_MESSAGE_TEMPLATE_KEY, model, event).thenAccept(interactionHook -> {
                log.info("Sent confirmation for command {} in server {} for user {}.", commandName, serverId, userId);
            }).exceptionally(throwable -> {
                log.warn("Failed to send confirmation for command {} in server {} for user {}.", commandName, serverId, userId);
                return null;
            });
            scheduleConfirmationDeletion(event.getIdLong(), confirmationId, abortId, serverId);
        } else {
            CompletableFuture<CommandResult> commandOutput;
            if(conditionResult.isResult()) {
                commandOutput = command.executeSlash(event).thenApply(commandResult -> {
                    log.info("Command {} in server {} was executed.", commandName, event.getGuild().getIdLong());
                    return commandResult;
                });
            } else {
                commandOutput = CompletableFuture.completedFuture(CommandResult.fromCondition(conditionResult));
            }
            commandOutput.thenAccept(commandResult -> {
                self.executePostCommandListener(command, event, commandResult);
            }).exceptionally(throwable -> {
                log.error("Error while handling post execution of command {}", commandName, throwable);
                CommandResult commandResult = CommandResult.fromError(throwable.getMessage(), throwable);
                self.executePostCommandListener(command, event, commandResult);
                return null;
            });
        }
    }

    private void scheduleConfirmationDeletion(Long interactionId, String confirmationPayloadId, String abortPayloadId, Long serverId) {
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("interactionId", interactionId.toString());
        parameters.put("confirmationPayloadId", confirmationPayloadId);
        parameters.put("abortPayloadId", abortPayloadId);
        JobParameters jobParameters = JobParameters
            .builder()
            .parameters(parameters)
            .build();
        Long confirmationTimeout = configService.getLongValueOrConfigDefault(CoreFeatureConfig.CONFIRMATION_TIMEOUT, serverId);
        Instant targetDate = Instant.now().plus(confirmationTimeout, ChronoUnit.SECONDS);
        log.info("Scheduling job to delete slash command confirmation in server {} at {}.", serverId, targetDate);
        schedulerService.executeJobWithParametersOnce("confirmationCleanupJob", "core", jobParameters, Date.from(targetDate));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executePostCommandListener(Command foundCommand, SlashCommandInteractionEvent event, CommandResult result) {
        for (PostCommandExecution postCommandExecution : executions) {
            postCommandExecution.executeSlash(event, result, foundCommand);
        }
    }

    private Optional<Command> findCommand(SlashCommandInteractionEvent event) {
        return commands
                .stream()
                .filter(command -> command.getConfiguration().getSlashCommandConfig().isEnabled())
                .filter(command -> command.getConfiguration().getSlashCommandConfig().matchesInteraction(event.getInteraction()))
                .findAny();
    }

    private Optional<Command> findCommand(CommandAutoCompleteInteractionEvent event) {
        return commands
                .stream()
                .filter(command -> command.getConfiguration().getSlashCommandConfig().isEnabled())
                .filter(command -> command.getConfiguration().getSlashCommandConfig().matchesInteraction(event.getInteraction()))
                .findAny();
    }

    @PostConstruct
    public void filterPostProcessors() {
        metricService.registerCounter(SLASH_COMMANDS_PROCESSED_COUNTER, "Slash Commands processed");
        executions = executions
                .stream()
                .filter(PostCommandExecution::supportsSlash)
                .collect(Collectors.toList());
    }
}
