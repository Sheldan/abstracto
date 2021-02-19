package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.service.PurgeService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Purge extends AbstractConditionableCommand {

    @Autowired
    private PurgeService purgeService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Integer amountOfMessages = (Integer) commandContext.getParameters().getParameters().get(0);
        Member memberToPurgeMessagesOf = null;
        if(commandContext.getParameters().getParameters().size() == 2) {
            memberToPurgeMessagesOf = (Member) commandContext.getParameters().getParameters().get(1);
        }
        return purgeService.purgeMessagesInChannel(amountOfMessages, commandContext.getChannel(), commandContext.getMessage(), memberToPurgeMessagesOf)
                .thenApply(aVoid -> CommandResult.fromSelfDestruct());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        List<ParameterValidator> messageAmountValidator = Arrays.asList(MinIntegerValueValidator.min(1L));
        parameters.add(Parameter.builder().name("amount").validators(messageAmountValidator).type(Integer.class).templated(true).build());
        parameters.add(Parameter.builder().name("member").type(Member.class).optional(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("purge")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MODERATION;
    }
}
