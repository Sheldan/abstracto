package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.utils.SnowflakeUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.PurgeService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Purge extends AbstractConditionableCommand {

    private static final String AMOUNT_PARAMETER = "amount";
    private static final String MEMBER_PARAMETER = "member";
    private static final String PURGE_COMMAND = "purge";
    private static final String PURGE_INITIAL_MESSAGE = "purge_initial_message";

    @Autowired
    private PurgeService purgeService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Integer amountOfMessages = (Integer) commandContext.getParameters().getParameters().get(0);
        Member memberToPurgeMessagesOf = null;
        if(commandContext.getParameters().getParameters().size() == 2) {
            memberToPurgeMessagesOf = (Member) commandContext.getParameters().getParameters().get(1);
            if(!memberToPurgeMessagesOf.getGuild().equals(commandContext.getGuild())) {
                throw new EntityGuildMismatchException();
            }
        }
        return purgeService.purgeMessagesInChannel(amountOfMessages, commandContext.getChannel(), commandContext.getMessage(), memberToPurgeMessagesOf)
                .thenApply(aVoid -> CommandResult.fromSelfDestruct());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Integer amountOfMessages = slashCommandParameterService.getCommandOption(AMOUNT_PARAMETER, event, Integer.class);
        Member memberToPurgeMessagesOf;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            memberToPurgeMessagesOf = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
            if(!memberToPurgeMessagesOf.getGuild().equals(event.getGuild())) {
                throw new EntityGuildMismatchException();
            }
        } else {
            memberToPurgeMessagesOf = null;
        }
        return interactionService.replyEmbed(PURGE_INITIAL_MESSAGE, event)
                .thenCompose(interactionHook -> {
                    Long startMessageId = SnowflakeUtils.createSnowFlake();
                    return purgeService.purgeMessagesInChannel(amountOfMessages, event.getGuildChannel(), startMessageId , event.getHook(), memberToPurgeMessagesOf);
                }).thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        List<ParameterValidator> messageAmountValidator = Arrays.asList(MinIntegerValueValidator.min(1L));
        Parameter amountParameter = Parameter
                .builder()
                .name(AMOUNT_PARAMETER)
                .validators(messageAmountValidator)
                .type(Integer.class)
                .templated(true)
                .build();
        parameters.add(amountParameter);
        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER)
                .type(Member.class)
                .optional(true)
                .templated(true)
                .build();
        parameters.add(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.MODERATION)
                .commandName(PURGE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(PURGE_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
