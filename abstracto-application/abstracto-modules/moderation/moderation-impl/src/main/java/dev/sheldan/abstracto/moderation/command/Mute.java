package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.command.MuteContext;
import dev.sheldan.abstracto.moderation.service.MuteService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.service.MuteService.MUTE_EFFECT_KEY;

@Component
public class Mute extends AbstractConditionableCommand {

    private static final String MUTE_DEFAULT_REASON_TEMPLATE = "mute_default_reason";
    private static final String DURATION_PARAMETER = "duration";
    private static final String MUTE_COMMAND = "mute";
    private static final String USER_PARAMETER = "user";
    private static final String REASON_PARAMETER = "reason";
    private static final String MUTE_RESPONSE = "mute_response";

    @Autowired
    private MuteService muteService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        Guild guild = commandContext.getGuild();
        GuildMessageChannel channel = commandContext.getChannel();
        if(!member.getGuild().equals(guild)) {
            throw new EntityGuildMismatchException();
        }
        Duration duration = (Duration) parameters.get(1);
        String defaultReason = templateService.renderSimpleTemplate(MUTE_DEFAULT_REASON_TEMPLATE, guild.getIdLong());
        String reason = parameters.size() == 3 ? (String) parameters.get(2) : defaultReason;
        ServerChannelMessage context = ServerChannelMessage
                .builder()
                .serverId(guild.getIdLong())
                .channelId(channel.getIdLong())
                .messageId(commandContext.getMessage().getIdLong())
                .build();
        MuteContext muteLogModel = MuteContext
                .builder()
                .muteDate(Instant.now())
                .muteTargetDate(Instant.now().plus(duration))
                .mutedUser(member)
                .reason(reason)
                .contextChannel(channel)
                .message(commandContext.getMessage())
                .mutingUser(commandContext.getAuthor())
                .context(context)
                .build();
        return muteService.muteMemberWithLog(muteLogModel)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        GuildMessageChannel channel = event.getGuildChannel();
        Member targetMember = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, Member.class);
        String durationStr = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, Duration.class, String.class);
        Duration duration = ParseUtils.parseDuration(durationStr);
        String reason;
        if(slashCommandParameterService.hasCommandOption(REASON_PARAMETER, event)) {
            reason = slashCommandParameterService.getCommandOption(REASON_PARAMETER, event, String.class);
        } else {
            reason = templateService.renderSimpleTemplate(MUTE_DEFAULT_REASON_TEMPLATE, guild.getIdLong());
        }
        ServerChannelMessage context = ServerChannelMessage
                .builder()
                .serverId(guild.getIdLong())
                .channelId(channel.getIdLong())
                .build();
        MuteContext muteLogModel = MuteContext
                .builder()
                .muteDate(Instant.now())
                .muteTargetDate(Instant.now().plus(duration))
                .mutedUser(targetMember)
                .reason(reason)
                .contextChannel(channel)
                .mutingUser(event.getMember())
                .context(context)
                .build();
        return muteService.muteMemberWithLog(muteLogModel)
                .thenCompose(unused -> interactionService.replyEmbed(MUTE_RESPONSE, event))
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

        Parameter durationParameter = Parameter
                .builder()
                .name(DURATION_PARAMETER)
                .templated(true)
                .type(Duration.class)
                .build();

        Parameter reasonParameter = Parameter
                .builder()
                .name(REASON_PARAMETER)
                .templated(true)
                .type(String.class)
                .optional(true)
                .remainder(true)
                .build();

        List<Parameter> parameters = Arrays.asList(userParameter, durationParameter, reasonParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        EffectConfig muteEffect = EffectConfig
                .builder()
                .position(0)
                .parameterName(USER_PARAMETER)
                .effectKey(MUTE_EFFECT_KEY)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.MUTE)
                .commandName("create")
                .build();

        List<EffectConfig> effectConfig = Arrays.asList(muteEffect);
        return CommandConfiguration.builder()
                .name(MUTE_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .effects(effectConfig)
                .causesReaction(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(immuneUserCondition);
        return conditions;
    }
}
