package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.models.LeaderBoard;
import dev.sheldan.abstracto.experience.models.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.models.templates.LeaderBoardModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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


    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        checkParameters(commandContext);
        List<Object> parameters = commandContext.getParameters().getParameters();
        // parameter is optional, in case its not present, we default to the 0th page
        Integer page = !parameters.isEmpty() ? (Integer) parameters.get(0) : 1;
        LeaderBoard leaderBoard = userExperienceService.findLeaderBoardData(commandContext.getUserInitiatedContext().getServer(), page);
        LeaderBoardModel leaderBoardModel = (LeaderBoardModel) ContextConverter.slimFromCommandContext(commandContext, LeaderBoardModel.class);
        List<CompletableFuture<LeaderBoardEntryModel>> futures = new ArrayList<>();
        List<CompletableFuture<LeaderBoardEntryModel>> completableFutures = converter.fromLeaderBoard(leaderBoard);
        futures.addAll(completableFutures);
        log.info("Rendering leaderboard for page {} in server {} for user {}.", page, commandContext.getAuthor().getId(), commandContext.getGuild().getId());

        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(commandContext.getUserInitiatedContext().getAUserInAServer());
        CompletableFuture<LeaderBoardEntryModel> userRankFuture = converter.fromLeaderBoardEntry(userRank);
        futures.add(userRankFuture);
        return FutureUtils.toSingleFutureGeneric(futures).thenCompose(aVoid -> {
            List<LeaderBoardEntryModel> finalModels = completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            leaderBoardModel.setUserExperiences(finalModels);
            leaderBoardModel.setUserExecuting(userRankFuture.join());
            MessageToSend messageToSend = templateService.renderEmbedTemplate(LEADER_BOARD_POST_EMBED_TEMPLATE, leaderBoardModel);
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()));
        }).thenApply(aVoid -> CommandResult.fromSuccess());

    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        List<ParameterValidator> leaderBoardPageValidators = Arrays.asList(MinIntegerValueValidator.min(0L));
        parameters.add(Parameter.builder().name("page").validators(leaderBoardPageValidators).optional(true).templated(true).type(Integer.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("leaderboard")
                .module(ExperienceModule.EXPERIENCE)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
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
