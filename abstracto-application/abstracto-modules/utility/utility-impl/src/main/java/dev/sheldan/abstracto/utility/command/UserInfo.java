package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.model.UserInfoModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class UserInfo extends AbstractConditionableCommand {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserInfo self;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member memberToShow = parameters.size() == 1 ? (Member) parameters.get(0) : commandContext.getAuthor();
        if(!memberToShow.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        UserInfoModel model = (UserInfoModel) ContextConverter.slimFromCommandContext(commandContext, UserInfoModel.class);
        if(!memberToShow.hasTimeJoined()) {
            log.info("Force reloading member {} in guild {} for user info.", memberToShow.getId(), memberToShow.getGuild().getId());
            return memberService.forceReloadMember(memberToShow).thenCompose(member -> {
                model.setMemberInfo(member);
                return self.sendResponse(commandContext, model)
                        .thenApply(aVoid -> CommandResult.fromIgnored());
            });
        } else {
            model.setMemberInfo(memberToShow);
            return self.sendResponse(commandContext, model)
                .thenApply(aVoid -> CommandResult.fromIgnored());
        }
    }

    @Transactional
    public CompletableFuture<Void> sendResponse(CommandContext commandContext, UserInfoModel model) {
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList("userInfo_response", model, commandContext.getChannel()));
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().type(Member.class).name("member").templated(true).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("userInfo")
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
