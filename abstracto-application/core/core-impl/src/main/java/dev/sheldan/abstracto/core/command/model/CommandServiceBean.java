package dev.sheldan.abstracto.core.command.model;

import com.google.common.collect.Iterables;
import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.condition.ConditionalCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.DriedCommandContext;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.AModule;
import dev.sheldan.abstracto.core.command.service.CommandRegistry;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.command.service.management.ModuleManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class CommandServiceBean implements CommandService {

    public static final String NO_FEATURE_COMMAND_FOUND_EXCEPTION_TEMPLATE = "no_feature_command_found_exception";
    private static final String[] MANDATORY_ENCLOSING = {"<", ">"};
    private static final String[] OPTIONAL_ENCLOSING = {"[", "]"};

    @Autowired
    private ModuleManagementService moduleManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private CommandReceivedHandler commandReceivedHandler;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MessageService messageService;

    @Override
    public ACommand createCommand(String name, String moduleName, FeatureDefinition featureDefinition) {
        AModule module = moduleManagementService.getOrCreate(moduleName);
        if(featureDefinition == null) {
            log.warn("Command {} in module {} has no feature.", name, moduleName);
            return null;
        }
        AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
        return commandManagementService.createCommand(name, module, feature);
    }

    @Override
    public boolean doesCommandExist(String name) {
        return commandManagementService.doesCommandExist(name);
    }

    @Override
    public void allowCommandForRole(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        if(commandForServer.getAllowedRoles().stream().noneMatch(role1 -> role1.getId().equals(role.getId()))) {
            commandForServer.getAllowedRoles().add(role);
        }
        log.info("Allowing command {} for role {} in server {}.", aCommand.getName(), role.getId(), role.getServer().getId());
        commandForServer.setRestricted(true);
    }

    @Override
    public void allowFeatureForRole(FeatureDefinition featureDefinition, ARole role) {
        AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
        feature.getCommands().forEach(command -> this.allowCommandForRole(command, role));
        log.info("Allowing feature {} for role {} in server {}.", feature.getKey(), role.getId(), role.getServer().getId());
    }

    @Override
    public void restrictCommand(ACommand aCommand, AServer server) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, server);
        commandForServer.setRestricted(true);
        log.info("Restricting command {} in server {}.", aCommand.getName(), server.getId());
    }

    @Override
    public String generateUsage(Command command) {
        StringBuilder builder = new StringBuilder();
        CommandConfiguration commandConfig = command.getConfiguration();
        builder.append(commandConfig.getName());
        if(!commandConfig.getParameters().isEmpty()) {
            builder.append(" ");
        }
        commandConfig.getParameters().forEach(parameter -> {
            if(parameter.getType().equals(File.class)) {
                return;
            }
            String[] enclosing = parameter.isOptional() ? OPTIONAL_ENCLOSING : MANDATORY_ENCLOSING;
            builder.append(enclosing[0]);
            builder.append(parameter.getName());
            builder.append(enclosing[1]);
            if(!parameter.equals(Iterables.getLast(commandConfig.getParameters()))) {
                builder.append(" ");
            }
        });
        return builder.toString();
    }

    @Override
    public void unRestrictCommand(ACommand aCommand, AServer server) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, server);
        commandForServer.setRestricted(false);
        log.info("Removing restriction on command {} in server {}.", aCommand.getName(), server.getId());
    }

    @Override
    public void disAllowCommandForRole(ACommand aCommand, ARole role) {
        ACommandInAServer commandForServer = commandInServerManagementService.getCommandForServer(aCommand, role.getServer());
        commandForServer.setRestricted(true);
        commandForServer.getAllowedRoles().removeIf(role1 -> role1.getId().equals(role.getId()));
        log.info("Disallowing command {} for role {} in server {}.", aCommand.getName(), role.getId(), role.getServer().getId());
    }

    @Override
    public void disAllowFeatureForRole(FeatureDefinition featureDefinition, ARole role) {
        AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
        feature.getCommands().forEach(command -> this.disAllowCommandForRole(command, role));
        log.info("Disallowing feature {} for role {} in server {}.", feature.getKey(), role.getId(), role.getServer().getId());
    }

    public CompletableFuture<ConditionResult> isCommandExecutable(Command command, CommandContext commandContext) {
        if(command instanceof ConditionalCommand) {
            ConditionalCommand castedCommand = (ConditionalCommand) command;
            return checkConditions(commandContext, command, castedCommand.getConditions());
        } else {
            return ConditionResult.fromAsyncSuccess();
        }
    }

    @Override
    public UnParsedCommandParameter getUnParsedCommandParameter(String messageContent, Message message) {
        return new UnParsedCommandParameter(messageContent, message);
    }

    @Override
    public CompletableFuture<Parameters> getParametersForCommand(String commandName, Message messageContainingContent) {
        String contentStripped = messageContainingContent.getContentRaw();
        UnParsedCommandParameter unParsedParameter = getUnParsedCommandParameter(contentStripped, messageContainingContent);
        Command command = commandRegistry.findCommandByParameters(commandName, unParsedParameter, messageContainingContent.getGuild().getIdLong());
        return commandReceivedHandler.getParsedParameters(unParsedParameter, command, messageContainingContent);
    }

    @Override
    public Parameter cloneParameter(Parameter parameter) {
        return Parameter
                .builder()
                .optional(parameter.isOptional())
                .type(parameter.getType())
                .remainder(parameter.isRemainder())
                .name(parameter.getName())
                .templated(parameter.getTemplated())
                .description(parameter.getDescription())
                .validators(parameter.getValidators())
                .isListParam(parameter.isListParam())
                .build();
    }

    private CompletableFuture<ConditionResult> checkConditions(CommandContext commandContext, Command command, List<CommandCondition> conditions) {
        if(conditions != null && !conditions.isEmpty()) {
            List<CompletableFuture<ConditionResult>> futures = new ArrayList<>();
            for (CommandCondition condition : conditions) {
                if(condition.isAsync()) {
                    futures.add(condition.shouldExecuteAsync(commandContext, command));
                } else {
                    futures.add(CompletableFuture.completedFuture(condition.shouldExecute(commandContext, command)));
                }
            }
            CompletableFuture<ConditionResult> resultFuture = new CompletableFuture<>();
            CompletableFutureList<ConditionResult> futureList = new CompletableFutureList<>(futures);
            futureList.getMainFuture().whenComplete((unused, throwable) -> {
                List<ConditionResult> results = futureList.getObjects();
                boolean foundResult = false;
                for (ConditionResult conditionResult : results) {
                    if (!conditionResult.isResult()) {
                        foundResult = true;
                        resultFuture.complete(conditionResult);
                        break;
                    }
                }
                if(!foundResult) {
                    resultFuture.complete(ConditionResult.fromSuccess());
                }
            }).exceptionally(throwable -> {
                resultFuture.completeExceptionally(throwable);
                return null;
            });
            return resultFuture;
        } else {
            return ConditionResult.fromAsyncSuccess();
        }
    }

    public CompletableFuture<RebuiltCommandContext> fillCommandContext(DriedCommandContext commandContext) {
        CompletableFuture<Member> memberFuture = memberService.getMemberInServerAsync(commandContext.getServerId(), commandContext.getUserId());
        CompletableFuture<Message> messageFuture = messageService.loadMessage(commandContext.getServerId(), commandContext.getChannelId(), commandContext.getMessageId());
        return CompletableFuture.allOf(memberFuture, messageFuture).thenCompose(unused -> {
            Message message = messageFuture.join();
            CommandReceivedHandler.UnParsedCommandResult unparsedResult = commandReceivedHandler.getUnparsedCommandResult(message);
            CompletableFuture<CommandReceivedHandler.CommandParseResult> getParseResult = commandReceivedHandler.getParametersFromMessage(message, unparsedResult);
            return getParseResult.thenApply(commandParseResult -> {
                Member author = memberFuture.join();
                UserInitiatedServerContext userInitiatedServerContext = UserInitiatedServerContext
                        .builder()
                        .messageChannel(message.getChannel())
                        .message(message)
                        .guild(message.getGuild())
                        .build();
                CommandContext context = CommandContext
                        .builder()
                        .channel(message.getTextChannel())
                        .author(author)
                        .guild(message.getGuild())
                        .jda(message.getJDA())
                        .parameters(commandParseResult.getParameters())
                        .userInitiatedContext(userInitiatedServerContext)
                        .message(message)
                        .build();
                return RebuiltCommandContext
                        .builder()
                        .context(context)
                        .command(unparsedResult.getCommand())
                        .build();
            });
        });
    }


    @Builder
    @Getter
    public static class RebuiltCommandContext {
        private CommandContext context;
        private Command command;
    }

}
