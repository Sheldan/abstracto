package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.model.BanResult.NOTIFICATION_FAILED;
import static dev.sheldan.abstracto.moderation.service.BanService.BAN_EFFECT_KEY;

@Component
@Slf4j
public class Ban extends AbstractConditionableCommand {

    private static final String BAN_COMMAND = "ban";
    private static final String REASON_PARAMETER = "reason";
    private static final String USER_PARAMETER = "user";
    public static final String BAN_NOTIFICATION_NOT_POSSIBLE = "ban_notification_not_possible";
    private static final String BAN_RESPONSE = "ban_response";

    @Autowired
    private BanService banService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private UserService userService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        User user = (User) parameters.get(0);
        String reason = (String) parameters.get(1);
        Guild guild = commandContext.getGuild();
        Message message = commandContext.getMessage();
        Member banningMember = commandContext.getAuthor();
        return banService.banUserWithNotification(user, reason, commandContext.getAuthor(), 0)
                .thenCompose(banResult -> {
                    if(banResult == NOTIFICATION_FAILED) {
                        String errorNotification = templateService.renderSimpleTemplate(BAN_NOTIFICATION_NOT_POSSIBLE, guild.getIdLong());
                        return channelService.sendTextToChannel(errorNotification, message.getChannel())
                                .thenAccept(message1 -> log.info("Notified about not being able to send ban notification in server {} and channel {} from user {}."
                                        , guild, message.getChannel().getIdLong(), banningMember.getIdLong()));
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String reason = slashCommandParameterService.getCommandOption(REASON_PARAMETER, event, String.class, String.class);
        if(slashCommandParameterService.hasCommandOptionWithFullType(USER_PARAMETER, event, OptionType.USER)) {
            Member member = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, User.class, Member.class);
            return banService.banUserWithNotification(member.getUser(), reason, event.getMember(), 0)
                    .thenCompose(banResult -> {
                        if(banResult == NOTIFICATION_FAILED) {
                            String errorNotification = templateService.renderSimpleTemplate(BAN_NOTIFICATION_NOT_POSSIBLE, event.getGuild().getIdLong());
                            return interactionService.replyString(errorNotification, event);
                        } else {
                            return interactionService.replyEmbed(BAN_RESPONSE, event);
                        }
                    })
                    .thenApply(aVoid -> CommandResult.fromSuccess());
        } else {
            String userIdStr = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, User.class, String.class);
            Long userId = Long.parseLong(userIdStr);
            return userService.retrieveUserForId(userId)
                    .thenCompose(user -> banService.banUserWithNotification(user, reason, event.getMember(), 0))
                    .thenCompose(banResult -> {
                        if(banResult == NOTIFICATION_FAILED) {
                            String errorNotification = templateService.renderSimpleTemplate(BAN_NOTIFICATION_NOT_POSSIBLE, event.getGuild().getIdLong());
                            return interactionService.replyString(errorNotification, event);
                        } else {
                            return interactionService.replyEmbed(BAN_RESPONSE, event);
                        }
                    })
                    .thenApply(banResult -> CommandResult.fromSuccess());
        }
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
                .remainder(true)
                .build();
        List<Parameter> parameters = Arrays.asList(userParameter, reasonParameter);
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
                .commandName(BAN_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(BAN_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
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
