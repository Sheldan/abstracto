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
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import dev.sheldan.abstracto.experience.models.templates.RankModel;
import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Rank extends AbstractConditionableCommand {

    public static final String RANK_POST_EMBED_TEMPLATE = "rank_post";
    @Autowired
    private LeaderBoardModelConverter converter;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private ExperienceLevelService experienceLevelService;

    @Autowired
    protected ChannelService channelService;


    @Override
    public CommandResult execute(CommandContext commandContext) {
        RankModel rankModel = (RankModel) ContextConverter.fromCommandContext(commandContext, RankModel.class);
        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(commandContext.getUserInitiatedContext().getAUserInAServer());
        rankModel.setRankUser(converter.fromLeaderBoardEntry(userRank));
        AUserExperience experienceObj = userRank.getExperience();
        rankModel.setExperienceToNextLevel(experienceLevelService.calculateExperienceToNextLevel(experienceObj.getCurrentLevel().getLevel(), experienceObj.getExperience()));
        MessageToSend messageToSend = templateService.renderEmbedTemplate(RANK_POST_EMBED_TEMPLATE, rankModel);
        channelService.sendMessageToEndInTextChannel(messageToSend, commandContext.getChannel());

        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("rank")
                .module(ExperienceModule.EXPERIENCE)
                .templated(true)
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
