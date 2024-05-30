package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadClosedException;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Sends the reply from the staff member to the user, but marks the reply as anonymous. The original author is still
 * tracked internally.
 */
@Component
public class AnonReply extends AbstractConditionableCommand {

    @Autowired
    private ModMailContextCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private UserService userService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // text is optional, for example if only an attachment is sent
        String text = parameters.size() == 1 ? (String) parameters.get(0) : "";
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(commandContext.getChannel().getIdLong());
        if(ModMailThreadState.CLOSED.equals(modMailThread.getState()) || ModMailThreadState.CLOSING.equals(modMailThread.getState())) {
            throw new ModMailThreadClosedException();
        }
        Long threadId = modMailThread.getId();
        return userService.retrieveUserForId(modMailThread.getUser().getUserReference().getId()).thenCompose(user ->
                modMailThreadService.loadExecutingMemberAndRelay(threadId, text, commandContext.getMessage(), true, user, commandContext.getGuild())
        ).thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter responseText = Parameter
                .builder()
                .name("text")
                .type(String.class)
                .remainder(true)
                .optional(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(responseText);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("anonReply")
                .async(true)
                .module(ModMailModuleDefinition.MODMAIL)
                .parameters(parameters)
                .messageCommandOnly(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
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
