package dev.sheldan.abstracto.utility.commands.repost;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.RepostDetectionModuleInterface;
import dev.sheldan.abstracto.utility.config.features.RepostDetectionFeatureMode;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.converter.RepostLeaderBoardConverter;
import dev.sheldan.abstracto.utility.models.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.utility.models.RepostLeaderboardModel;
import dev.sheldan.abstracto.utility.models.database.result.RepostLeaderboardResult;
import dev.sheldan.abstracto.utility.service.RepostService;
import dev.sheldan.abstracto.utility.service.management.RepostManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RepostLeaderboard extends AbstractConditionableCommand {

    public static final String REPOST_LEADERBOARD_RESPONSE_TEMPLATE_KEY = "repostLeaderboard_response";
    @Autowired
    private RepostService repostService;

    @Autowired
    private RepostManagementService repostManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private RepostLeaderBoardConverter converter;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Integer page = !parameters.isEmpty() ? (Integer) parameters.get(0) : 1;
        AUserInAServer aUserInAServer = userInServerManagementService.loadUser(commandContext.getAuthor());
        List<RepostLeaderboardResult> topRepostingUsersOfServer = repostManagementService.findTopRepostingUsersOfServer(commandContext.getGuild().getIdLong(), page, 5);
        RepostLeaderboardResult resultOfUser = repostManagementService.getRepostRankOfUser(aUserInAServer);
        CompletableFuture<List<RepostLeaderboardEntryModel>> leaderBoardFuture = converter.fromLeaderBoardResults(topRepostingUsersOfServer);
        CompletableFuture<RepostLeaderboardEntryModel> userFuture = converter.convertSingleUser(resultOfUser);
        return CompletableFuture.allOf(leaderBoardFuture, userFuture).thenCompose(unused -> {
            List<RepostLeaderboardEntryModel> entries = leaderBoardFuture.join();
            RepostLeaderboardModel model = RepostLeaderboardModel
                    .builder()
                    .guild(commandContext.getGuild())
                    .entries(entries)
                    .userExecuting(userFuture.join())
                    .member(commandContext.getAuthor())
                    .build();
            return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(REPOST_LEADERBOARD_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()));
        }).thenApply(o -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelToSet = Parameter.builder().name("page").type(Integer.class).templated(true).optional(true).build();
        List<Parameter> parameters = Arrays.asList(channelToSet);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("repostLeaderboard")
                .module(RepostDetectionModuleInterface.REPOST_DETECTION)
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
        return UtilityFeature.REPOST_DETECTION;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(RepostDetectionFeatureMode.LEADERBOARD);
    }
}
