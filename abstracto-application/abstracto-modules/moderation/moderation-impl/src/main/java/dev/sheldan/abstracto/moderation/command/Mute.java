package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.EffectConfig;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.template.command.MuteContext;
import dev.sheldan.abstracto.moderation.service.MuteService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.service.MuteService.MUTE_EFFECT_KEY;

@Component
public class Mute extends AbstractConditionableCommand {

    @Autowired
    private MuteService muteService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        Duration duration = (Duration) parameters.get(1);
        String reason = (String) parameters.get(2);
        ServerChannelMessage context = ServerChannelMessage
                .builder()
                .serverId(commandContext.getGuild().getIdLong())
                .channelId(commandContext.getChannel().getIdLong())
                .messageId(commandContext.getMessage().getIdLong())
                .build();
        MuteContext muteLogModel = MuteContext
                .builder()
                .muteDate(Instant.now())
                .muteTargetDate(Instant.now().plus(duration))
                .mutedUser(member)
                .reason(reason)
                .contextChannel(commandContext.getChannel())
                .message(commandContext.getMessage())
                .mutingUser(commandContext.getAuthor())
                .context(context)
                .build();
        return muteService.muteMemberWithLog(muteLogModel)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").templated(true).type(Member.class).build());
        parameters.add(Parameter.builder().name("duration").templated(true).type(Duration.class).build());
        parameters.add(Parameter.builder().name("reason").templated(true).type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        List<EffectConfig> effectConfig = Arrays.asList(EffectConfig.builder().position(0).effectKey(MUTE_EFFECT_KEY).build());
        return CommandConfiguration.builder()
                .name("mute")
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
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
