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
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.ShowAvatarModel;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShowAvatar extends AbstractConditionableCommand {

    public static final String SHOW_AVATAR_RESPONSE_TEMPLATE = "showAvatar_response";
    @Autowired
    private ChannelService channelService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        checkParameters(commandContext);
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member memberToShow = parameters.size() == 1 ? (Member) parameters.get(0) : commandContext.getUserInitiatedContext().getMember();
        ShowAvatarModel model = (ShowAvatarModel) ContextConverter.fromCommandContext(commandContext, ShowAvatarModel.class);
        model.setMemberInfo(memberToShow);
        channelService.sendEmbedTemplateInChannel(SHOW_AVATAR_RESPONSE_TEMPLATE, model, commandContext.getChannel());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().type(Member.class).name("member").templated(true).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("showAvatar")
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