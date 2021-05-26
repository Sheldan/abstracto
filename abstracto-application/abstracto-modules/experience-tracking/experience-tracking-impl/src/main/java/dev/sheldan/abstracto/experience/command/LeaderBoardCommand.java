package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Shows the experience gain information of the top 10 users in the server, or if a page number is provided as a parameter, only the members which are on this page.
 */
@Component
@Slf4j
public class LeaderBoardCommand extends AbstractConditionableCommand {

    public static final String LEADER_BOARD_POST_EMBED_TEMPLATE = "leaderboard_post";
    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private LeaderBoardModelConverter converter;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // parameter is optional, in case its not present, we default to the 0th page
        Integer page = !parameters.isEmpty() ? (Integer) parameters.get(0) : 1;
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        LeaderBoard leaderBoard = userExperienceService.findLeaderBoardData(server, page);
        LeaderBoardModel leaderBoardModel = (LeaderBoardModel) ContextConverter.slimFromCommandContext(commandContext, LeaderBoardModel.class);
        List<CompletableFuture> futures = new ArrayList<>();
        CompletableFuture<List<LeaderBoardEntryModel>> completableFutures = converter.fromLeaderBoard(leaderBoard);
        futures.add(completableFutures);
        log.info("Rendering leaderboard for page {} in server {} for user {}.", page, commandContext.getAuthor().getId(), commandContext.getGuild().getId());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(commandContext.getAuthor());
        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(aUserInAServer);
        CompletableFuture<List<LeaderBoardEntryModel>> userRankFuture = converter.fromLeaderBoardEntry(Arrays.asList(userRank));
        futures.add(userRankFuture);
        return FutureUtils.toSingleFuture(futures).thenCompose(aVoid -> {
            List<LeaderBoardEntryModel> finalModels = completableFutures.join();
            leaderBoardModel.setUserExperiences(finalModels);
            leaderBoardModel.setUserExecuting(userRankFuture.join().get(0));
            MessageToSend messageToSend = templateService.renderEmbedTemplate(LEADER_BOARD_POST_EMBED_TEMPLATE, leaderBoardModel, commandContext.getGuild().getIdLong());
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()));
        }).thenApply(aVoid -> CommandResult.fromIgnored());

    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        List<ParameterValidator> leaderBoardPageValidators = Arrays.asList(MinIntegerValueValidator.min(0L));
        parameters.add(Parameter.builder().name("page").validators(leaderBoardPageValidators).optional(true).templated(true).type(Integer.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("leaderboard")
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
