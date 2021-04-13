package dev.sheldan.abstracto.entertainment.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ReactionService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.exception.ReactTooManyReactionsException;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class React extends AbstractConditionableCommand {

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private ReactionService reactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Message message = (Message) parameters.get(0);
        String text = (String) parameters.get(1);

        List<String> reactionChars = entertainmentService.convertTextToEmojis(text);
        int existingReactions = message.getReactions().size();
        if(reactionChars.size() + existingReactions > Message.MAX_REACTIONS) {
            throw new ReactTooManyReactionsException();
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        reactionChars.forEach(s -> futures.add(reactionService.addDefaultReactionToMessageAsync(s, message)));
        return FutureUtils.toSingleFutureGeneric(futures)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("message").type(Message.class).templated(true).build());
        parameters.add(Parameter.builder().name("text").type(String.class).remainder(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("react")
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .causesReaction(true)
                .async(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
    }
}
