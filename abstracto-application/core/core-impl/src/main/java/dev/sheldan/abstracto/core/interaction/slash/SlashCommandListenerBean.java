package dev.sheldan.abstracto.core.interaction.slash;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
    private SlashCommandListenerBean self;

    @Autowired
    private CommandService commandService;

    @Autowired
    private List<PostCommandExecution> executions;

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
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(commands == null || commands.isEmpty()) return;
        CompletableFuture.runAsync(() ->  self.executeListenerLogic(event), slashCommandExecutor).exceptionally(throwable -> {
            log.error("Failed to execute listener logic in async button event.", throwable);
            return null;
        });
    }

    @Transactional
    public void executeListenerLogic(SlashCommandInteractionEvent event) {
        Optional<Command> potentialCommand = findCommand(event);
        potentialCommand.ifPresent(command -> {
            try {
                commandService.isCommandExecutable(command, event).thenAccept(conditionResult -> {
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

    @Transactional
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

    @PostConstruct
    public void filterPostProcessors() {
        executions = executions
                .stream()
                .filter(PostCommandExecution::supportsSlash)
                .collect(Collectors.toList());
    }
}
