package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.config.UtilitySlashCommandNames;
import dev.sheldan.abstracto.utility.model.UserInfoModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class UserInfo extends AbstractConditionableCommand {

    public static final String USER_INFO_COMMAND = "userInfo";
    public static final String MEMBER_PARAMETER = "member";
    public static final String USER_INFO_RESPONSE = "userInfo_response";
    @Autowired
    private ChannelService channelService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserInfo self;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member memberToShow = parameters.size() == 1 ? (Member) parameters.get(0) : commandContext.getAuthor();
        if(!memberToShow.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        UserInfoModel model = UserInfoModel
                .builder()
                .build();
        if(!memberToShow.hasTimeJoined()) {
            log.info("Force reloading member {} in guild {} for user info.", memberToShow.getId(), memberToShow.getGuild().getId());
            return memberService.forceReloadMember(memberToShow).thenCompose(member -> {
                model.setMemberInfo(member);
                model.setCreationDate(member.getTimeCreated().toInstant());
                model.setJoinDate(member.getTimeJoined().toInstant());
                return self.sendResponse(commandContext.getChannel(), model)
                        .thenApply(aVoid -> CommandResult.fromIgnored());
            });
        } else {
            model.setMemberInfo(memberToShow);
            model.setCreationDate(memberToShow.getTimeCreated().toInstant());
            model.setJoinDate(memberToShow.getTimeJoined().toInstant());
            return self.sendResponse(commandContext.getChannel(), model)
                .thenApply(aVoid -> CommandResult.fromIgnored());
        }
    }

    @Transactional
    public CompletableFuture<Void> sendResponse(MessageChannel channel, UserInfoModel model) {
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(USER_INFO_RESPONSE, model, channel));
    }

    @Transactional
    public CompletableFuture<InteractionHook> sendResponse(IReplyCallback callback, UserInfoModel model) {
        return interactionService.replyEmbed(USER_INFO_RESPONSE, model, callback);
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member memberToShow;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            memberToShow = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
        } else {
            memberToShow = event.getMember();
        }
        if(!memberToShow.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        UserInfoModel model = UserInfoModel
                .builder()
                .build();
        if(!memberToShow.hasTimeJoined()) {
            log.info("Force reloading member {} in guild {} for user info.", memberToShow.getId(), memberToShow.getGuild().getId());
            return memberService.forceReloadMember(memberToShow).thenCompose(member -> {
                model.setMemberInfo(member);
                model.setCreationDate(member.getTimeCreated().toInstant());
                model.setJoinDate(member.getTimeJoined().toInstant());
                return self.sendResponse(event, model)
                        .thenApply(aVoid -> CommandResult.fromIgnored());
            });
        } else {
            model.setMemberInfo(memberToShow);
            model.setCreationDate(memberToShow.getTimeCreated().toInstant());
            model.setJoinDate(memberToShow.getTimeJoined().toInstant());
            return self.sendResponse(event, model)
                    .thenApply(aVoid -> CommandResult.fromIgnored());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter memberParameter = Parameter
                .builder()
                .type(Member.class)
                .name(MEMBER_PARAMETER)
                .templated(true)
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
                .rootCommandName(UtilitySlashCommandNames.UTILITY)
                .commandName(USER_INFO_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(USER_INFO_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
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
