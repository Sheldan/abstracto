package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.model.template.RankModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to show an embed containing information about the experience amount, level and message count of a ember on a server
 */
@Component
@Slf4j
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
    private ChannelService channelService;

    @Autowired
    private Rank self;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member parameter = !parameters.isEmpty() ? (Member) parameters.get(0) : commandContext.getAuthor();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(parameter);
        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(aUserInAServer);
        CompletableFuture<LeaderBoardEntryModel> future = converter.fromLeaderBoardEntry(userRank);
        RankModel rankModel = RankModel
                .builder()
                .member(parameter)
                .build();
        return future.thenCompose(leaderBoardEntryModel ->
            self.renderAndSendRank(commandContext, rankModel, leaderBoardEntryModel)
        ).thenApply(result -> CommandResult.fromIgnored());
    }

    @Transactional
    public CompletableFuture<Void> renderAndSendRank(CommandContext commandContext, RankModel rankModel, LeaderBoardEntryModel leaderBoardEntryModel) {
        rankModel.setRankUser(leaderBoardEntryModel);
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(commandContext.getAuthor());
        AUserExperience experienceObj = userExperienceManagementService.findUserInServer(aUserInAServer);
        log.info("Rendering rank for user {} in server {}.", commandContext.getAuthor().getId(), commandContext.getGuild().getId());
        rankModel.setExperienceToNextLevel(experienceLevelService.calculateExperienceToNextLevel(experienceObj.getCurrentLevel().getLevel(), experienceObj.getExperience()));
        MessageToSend messageToSend = templateService.renderEmbedTemplate(RANK_POST_EMBED_TEMPLATE, rankModel, commandContext.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()));
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("member").templated(true).type(Member.class).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("rank")
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
