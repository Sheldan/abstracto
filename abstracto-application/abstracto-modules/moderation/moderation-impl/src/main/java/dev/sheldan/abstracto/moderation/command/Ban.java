package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.transaction.annotation.Transactional;

import static dev.sheldan.abstracto.moderation.model.BanResult.NOTIFICATION_FAILED;
import static dev.sheldan.abstracto.moderation.service.BanService.BAN_EFFECT_KEY;

@Component
@Slf4j
public class Ban extends AbstractConditionableCommand {

    private static final String BAN_COMMAND = "ban";
    private static final String REASON_PARAMETER = "reason";
    private static final String DELETION_DURATION_PARAMETER = "deletionDuration";
    private static final String USER_PARAMETER = "user";
    public static final String BAN_NOTIFICATION_NOT_POSSIBLE = "ban_notification_not_possible";
    private static final String BAN_RESPONSE = "ban_response";

    @Autowired
    private BanService banService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private UserService userService;

    @Autowired
    private Ban self;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String reason = slashCommandParameterService.getCommandOption(REASON_PARAMETER, event, String.class, String.class);
        Duration duration;
        if(slashCommandParameterService.hasCommandOption(DELETION_DURATION_PARAMETER, event)) {
            String durationString = slashCommandParameterService.getCommandOption(DELETION_DURATION_PARAMETER, event, Duration.class, String.class);
            duration = ParseUtils.parseDuration(durationString);
        } else {
            duration = null;
        }

        Member member = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, User.class, Member.class);
        if(member != null) {
            return event.deferReply().submit()
                .thenCompose((hook) -> self.banMember(event, member, reason, duration, hook))
                .thenApply(commandResult -> CommandResult.fromSuccess());
        } else {
            User user = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, User.class, User.class);
            return event.deferReply().submit()
                .thenCompose((hook) -> self.banViaUserId(event, user.getIdLong(), reason, duration, hook))
                .thenApply(commandResult -> CommandResult.fromSuccess());
        }
    }

    @Transactional
    public CompletableFuture<Void> banViaUserId(SlashCommandInteractionEvent event, Long userId, String reason,
                                                                               Duration duration, InteractionHook hook) {
        return userService.retrieveUserForId(userId)
            .thenCompose(user -> banService.banUserWithNotification(ServerUser.fromId(event.getGuild().getIdLong(), userId), reason,
                ServerUser.fromId(event.getGuild().getIdLong(), event.getUser().getIdLong()), event.getGuild(), duration))
            .thenCompose(banResult -> {
                if (banResult == NOTIFICATION_FAILED) {
                    String errorNotification = templateService.renderSimpleTemplate(BAN_NOTIFICATION_NOT_POSSIBLE, event.getGuild().getIdLong());
                    return interactionService.replyString(errorNotification, hook)
                        .thenAccept(message -> {
                        });
                } else {
                    return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(BAN_RESPONSE, new Object(), hook));
                }
            });
    }

    @Transactional
    public CompletableFuture<Void> banMember(SlashCommandInteractionEvent event, Member member, String reason,
                                                                               Duration duration, InteractionHook hook) {
        return banService.banUserWithNotification(ServerUser.fromMember(member), reason,
                ServerUser.fromId(event.getGuild().getIdLong(), event.getUser().getIdLong()), event.getGuild(),
                duration)
            .thenCompose(banResult -> {
                if (banResult == NOTIFICATION_FAILED) {
                    String errorNotification = templateService.renderSimpleTemplate(BAN_NOTIFICATION_NOT_POSSIBLE, event.getGuild().getIdLong());
                    return interactionService.replyString(errorNotification, hook)
                        .thenAccept(message -> {
                        });
                } else {
                    return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(BAN_RESPONSE, new Object(), hook));
                }
            });
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter userParameter = Parameter
                .builder()
                .name(USER_PARAMETER)
                .templated(true)
                .type(User.class)
                .build();
        Parameter reasonParameter = Parameter
                .builder()
                .name(REASON_PARAMETER)
                .templated(true)
                .type(String.class)
                .build();

        Parameter deletionDurationParameter = Parameter
                .builder()
                .name(DELETION_DURATION_PARAMETER)
                .templated(true)
                .type(String.class)
                .optional(true)
                .build();

        List<Parameter> parameters = Arrays.asList(userParameter, reasonParameter, deletionDurationParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();
        EffectConfig banEffect = EffectConfig
                .builder()
                .position(0)
                .parameterName(USER_PARAMETER)
                .effectKey(BAN_EFFECT_KEY)
                .build();
        List<EffectConfig> effectConfig = Arrays.asList(banEffect);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.MODERATION)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName(BAN_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(BAN_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .effects(effectConfig)
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

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(immuneUserCondition);
        return conditions;
    }
}
