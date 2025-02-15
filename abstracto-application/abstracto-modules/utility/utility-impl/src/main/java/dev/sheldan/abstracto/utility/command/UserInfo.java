package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.handler.parameter.CombinedParameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.config.UtilitySlashCommandNames;
import dev.sheldan.abstracto.utility.model.UserInfoModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.command.config.Parameter.ADDITIONAL_TYPES_KEY;

@Component
@Slf4j
public class UserInfo extends AbstractConditionableCommand {

    public static final String USER_INFO_COMMAND = "userInfo";
    public static final String MEMBER_PARAMETER = "member";
    public static final String USER_INFO_RESPONSE = "userInfo_response";

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserInfo self;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Transactional
    public CompletableFuture<InteractionHook> sendResponse(IReplyCallback callback, UserInfoModel model) {
        return interactionService.replyEmbed(USER_INFO_RESPONSE, model, callback);
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        boolean userCommand = ContextUtils.isUserCommandInGuild(event);
        boolean knownGuild = ContextUtils.isGuildKnown(event);
        UserInfoModel model = UserInfoModel
                .builder()
                .build();
        if(knownGuild) {
            Member memberToShow;
            if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
                memberToShow = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
            } else {
                memberToShow = event.getMember();
            }
            if(!memberToShow.getGuild().equals(event.getGuild())) {
                throw new EntityGuildMismatchException();
            }
            if(!memberToShow.hasTimeJoined()) {
                log.info("Force reloading member {} in guild {} for user info.", memberToShow.getId(), memberToShow.getGuild().getId());
                return memberService.forceReloadMember(memberToShow).thenCompose(member -> {
                    fillUserInfoModel(model, member, userCommand);
                    return self.sendResponse(event, model)
                            .thenApply(aVoid -> CommandResult.fromIgnored());
                });
            } else {
                fillUserInfoModel(model, memberToShow, userCommand);
                return self.sendResponse(event, model)
                        .thenApply(aVoid -> CommandResult.fromIgnored());
            }
        } else {
            User targetUser;
            if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
                targetUser = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, User.class);
            } else {
                targetUser = event.getUser();
            }
            fillUserInfoModel(model, targetUser);
            return self.sendResponse(event, model)
                    .thenApply(aVoid -> CommandResult.fromIgnored());
        }

    }

    private void fillUserInfoModel(UserInfoModel model, User targetUser) {
        model.setCreationDate(targetUser.getTimeCreated().toInstant());
        model.setId(targetUser.getIdLong());
        MemberNameDisplay memberDisplay = MemberNameDisplay
                .builder()
                .userName(targetUser.getName())
                .userAvatarUrl(targetUser.getEffectiveAvatarUrl())
                .discriminator(targetUser.getDiscriminator())
                .displayName(targetUser.getGlobalName())
                .build();
        model.setMemberDisplay(memberDisplay);
    }

    private void fillUserInfoModel(UserInfoModel model, Member member, boolean userCommand) {
        model.setCreationDate(member.getTimeCreated().toInstant());
        model.setJoinDate(member.getTimeJoined().toInstant());
        model.setId(member.getIdLong());
        model.setMemberDisplay(MemberNameDisplay.fromMember(member));
        if(!userCommand) {
            model.setOnlineStatus(member.getOnlineStatus().getKey());
            member.getRoles().forEach(role -> model.getRoles().add(RoleDisplay.fromRole(role)));
            member.getActivities().forEach(activity -> model.getActivities().add(activity.getType().name()));
            Optional<Activity> customStatusOptional = member.getActivities().stream().filter(activity -> activity.getType().equals(Activity.ActivityType.CUSTOM_STATUS)).findFirst();
            customStatusOptional.ifPresent(activity -> {
                model.setCustomStatus(activity.getName());
                model.setCustomEmoji(activity.getEmoji() != null ? activity.getEmoji().getFormatted() : null);
            });
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Map<String, Object> parameterAlternatives = new HashMap<>();
        parameterAlternatives.put(ADDITIONAL_TYPES_KEY, List.of(
                CombinedParameterEntry.messageParameter(Message.class),
                CombinedParameterEntry.parameter(Member.class),
                CombinedParameterEntry.parameter(User.class)));
        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER)
                .type(CombinedParameter.class)
                .additionalInfo(parameterAlternatives)
                .templated(true)
                .useStrictParameters(true)
                .optional(true)
                .build();
        parameters.add(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.guildOnly())
                .rootCommandName(UtilitySlashCommandNames.UTILITY)
                .commandName(USER_INFO_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(USER_INFO_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return UtilityFeatureDefinition.UTILITY;
    }
}
