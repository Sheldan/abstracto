package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public interface CommandService {
    ACommand createCommand(String name, String moduleName, FeatureDefinition featureDefinition);
    boolean doesCommandExist(String name);
    void allowCommandForRole(ACommand aCommand, ARole role);
    void allowFeatureForRole(FeatureDefinition featureDefinition, ARole role);
    void restrictCommand(ACommand aCommand, AServer server);
    String generateUsage(Command command);
    void unRestrictCommand(ACommand aCommand, AServer server);
    void disAllowCommandForRole(ACommand aCommand, ARole role);
    void disAllowFeatureForRole(FeatureDefinition featureDefinition, ARole role);
    CompletableFuture<ConditionResult> isCommandExecutable(Command command, CommandContext commandContext);
    CompletableFuture<ConditionResult> isCommandExecutable(Command command, SlashCommandInteractionEvent slashCommandInteractionEvent);
    UnParsedCommandParameter getUnParsedCommandParameter(String messageContent, Message message);
    CompletableFuture<Parameters> getParametersForCommand(String commandName, Message messageContainingContent);
    Parameter cloneParameter(Parameter parameter);
}
