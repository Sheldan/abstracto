package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LeaderBoardCommand extends AbstractConditionableCommand {

    public static final String LEADERBOARD_POST_EMBED_TEMPLATE = "leaderboard_post";
    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private LeaderBoardModelConverter converter;


    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // parameter is optional, in case its not present, we default to the 0th page
        Integer page = !parameters.isEmpty() ? (Integer) parameters.get(0) : 0;
        LeaderBoard leaderBoard = userExperienceService.findLeaderBoardData(commandContext.getUserInitiatedContext().getServer(), page);
        LeaderBoardModel leaderBoardModel = (LeaderBoardModel) ContextConverter.fromCommandContext(commandContext, LeaderBoardModel.class);
        leaderBoardModel.setUserExperiences(converter.fromLeaderBoard(leaderBoard));

        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(commandContext.getUserInitiatedContext().getAUserInAServer());
        leaderBoardModel.setUserExecuting(converter.fromLeaderBoardEntry(userRank));
        MessageToSend messageToSend = templateService.renderEmbedTemplate(LEADERBOARD_POST_EMBED_TEMPLATE, leaderBoardModel);
        channelService.sendMessageToEndInTextChannel(messageToSend, commandContext.getChannel());

        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("page").optional(true).type(Integer.class).build());
        HelpInfo helpInfo = HelpInfo.builder().longHelp("Shows the leaderboard, first 10 places or given page.").usage("leaderboard").build();
        return CommandConfiguration.builder()
                .name("leaderboard")
                .module(ExperienceModule.EXPERIENCE)
                .description("Shows the leaderboard, first 10 places or given page.")
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }
}
