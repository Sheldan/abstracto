package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.experience.config.ExperienceFeatures;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.RankModel;
import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import dev.sheldan.abstracto.experience.service.ExperienceTrackerService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Rank extends AbstractConditionableCommand {

    @Autowired
    private LeaderBoardModelConverter converter;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ExperienceTrackerService experienceTrackerService;

    @Autowired
    private ExperienceLevelService experienceLevelService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        RankModel rankModel = (RankModel) ContextConverter.fromCommandContext(commandContext, RankModel.class);
        LeaderBoardEntry userRank = experienceTrackerService.getRankOfUserInServer(commandContext.getUserInitiatedContext().getAUserInAServer());
        rankModel.setRankUser(converter.fromLeaderBoardEntry(userRank));
        AUserExperience experienceObj = userRank.getExperience();
        rankModel.setExperienceToNextLevel(experienceLevelService.calculateExperienceToNextLevel(experienceObj.getCurrentLevel().getLevel(), experienceObj.getExperience()));
        MessageToSend messageToSend = templateService.renderEmbedTemplate("rank_post", rankModel);
        channelService.sendMessageToEndInTextChannel(messageToSend, commandContext.getChannel());

        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().longHelp("Shows the leaderboard, first 10 places or given page.").usage("rank").build();
        return CommandConfiguration.builder()
                .name("rank")
                .module(ExperienceModule.EXPERIENCE)
                .description("Shows your experience, rank and level on this server.")
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return ExperienceFeatures.EXPERIENCE;
    }
}
