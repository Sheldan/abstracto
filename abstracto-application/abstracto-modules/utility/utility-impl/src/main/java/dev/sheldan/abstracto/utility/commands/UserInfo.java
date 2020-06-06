package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.UserInfoModel;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserInfo extends AbstractConditionableCommand {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private BotService botService;

    @Autowired
    private UserInfo self;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        checkParameters(commandContext);
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member memberToShow = parameters.size() == 1 ? (Member) parameters.get(0) : commandContext.getAuthor();
        UserInfoModel model = (UserInfoModel) ContextConverter.fromCommandContext(commandContext, UserInfoModel.class);
        if(!memberToShow.hasTimeJoined()) {
            botService.forceReloadMember(memberToShow).thenAccept(member -> {
                model.setMemberInfo(member);
                self.sendResponse(commandContext, model);
            });
        } else {
            model.setMemberInfo(memberToShow);
            self.sendResponse(commandContext, model);
        }
        return CommandResult.fromSuccess();
    }

    @Transactional
    public void sendResponse(CommandContext commandContext, UserInfoModel model) {
        channelService.sendEmbedTemplateInChannel("userInfo_response", model, commandContext.getChannel());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().type(Member.class).name("member").templated(true).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("userInfo")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.UTILITY;
    }
}
