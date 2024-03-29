package dev.sheldan.abstracto.core.interaction.slash;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    @Transactional(rollbackFor = AbstractoRunTimeException.class)
    public void executeCommand(SlashCommandInteractionEvent event, Command command, ConditionResult conditionResult) {
        CompletableFuture<CommandResult> commandOutput;
        if(conditionResult.isResult()) {
            commandOutput = command.executeSlash(event).thenApply(commandResult -> {
                log.info("Command {} in server {} was executed.", command.getConfiguration().getName(), event.getGuild().getIdLong());
                return commandResult;
            });
        } else {
            commandOutput = CompletableFuture.completedFuture(CommandResult.fromCondition(conditionResult));
        }
        commandOutput.thenAccept(commandResult -> {
            self.executePostCommandListener(command, event, commandResult);
        }).exceptionally(throwable -> {
            log.error("Error while handling post execution of command {}", command.getConfiguration().getName(), throwable);
            CommandResult commandResult = CommandResult.fromError(throwable.getMessage(), throwable);
            self.executePostCommandListener(command, event, commandResult);
            return null;
        });
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
