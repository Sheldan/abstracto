package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import dev.sheldan.abstracto.moderation.service.KickServiceBean;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.service.KickService.KICK_EFFECT_KEY;

@Component
public class Kick extends AbstractConditionableCommand {

    public static final String KICK_DEFAULT_REASON_TEMPLATE = "kick_default_reason";
    public static final String KICK_COMMAND = "kick";
    public static final String USER_PARAMETER = "user";
    public static final String REASON_PARAMETER = "reason";
    private static final String KICK_RESPONSE = "kick_response";
    @Autowired
    private TemplateService templateService;

    @Autowired
    private KickServiceBean kickService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        if(!member.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        String defaultReason = templateService.renderSimpleTemplate(KICK_DEFAULT_REASON_TEMPLATE, commandContext.getGuild().getIdLong());
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;

        KickLogModel kickLogModel = KickLogModel
                .builder()
                .kickedUser(member)
                .reason(reason)
                .guild(commandContext.getGuild())
                .channel(commandContext.getChannel())
                .member(commandContext.getAuthor())
                .build();
        kickLogModel.setKickedUser(member);
        kickLogModel.setReason(reason);
        return kickService.kickMember(member, reason, kickLogModel)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member member = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, Member.class);
        if(!member.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        String reason;
        if(slashCommandParameterService.hasCommandOption(REASON_PARAMETER, event)) {
            reason = slashCommandParameterService.getCommandOption(REASON_PARAMETER, event, String.class);
        } else {
            reason = templateService.renderSimpleTemplate(KICK_DEFAULT_REASON_TEMPLATE, event.getGuild().getIdLong());
        }

        KickLogModel kickLogModel = KickLogModel
                .builder()
                .kickedUser(member)
                .reason(reason)
                .guild(event.getGuild())
                .channel(event.getGuildChannel())
                .member(event.getMember())
                .build();
        kickLogModel.setKickedUser(member);
        kickLogModel.setReason(reason);
        return kickService.kickMember(member, reason, kickLogModel)
                .thenCompose(unused -> interactionService.replyEmbed(KICK_RESPONSE, event))
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter userParameter = Parameter
                .builder()
                .name(USER_PARAMETER)
                .templated(true)
                .type(Member.class)
                .build();
        Parameter reasonParameter = Parameter
                .builder()
                .name(REASON_PARAMETER)
                .templated(true)
                .type(String.class)
                .optional(true)
                .remainder(true)
                .build();
        List<Parameter> parameters = Arrays.asList(userParameter, reasonParameter);
        EffectConfig kickEffect = EffectConfig
                .builder()
                .position(0)
                .parameterName(USER_PARAMETER)
                .effectKey(KICK_EFFECT_KEY)
                .build();
        List<EffectConfig> effectConfig = Arrays.asList(kickEffect);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.MODERATION)
                .commandName(KICK_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(KICK_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .async(true)
                .effects(effectConfig)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(immuneUserCondition);
        return conditions;
    }
}
