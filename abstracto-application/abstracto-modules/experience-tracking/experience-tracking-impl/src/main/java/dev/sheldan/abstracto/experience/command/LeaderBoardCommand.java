package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.model.LeaderBoard;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
    private static final String LEADERBOARD_PARAMTER = "leaderboard";
    private static final String PAGE_PARAMETER = "page";
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

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // parameter is optional, in case its not present, we default to the 0th page
        Integer page = !parameters.isEmpty() ? (Integer) parameters.get(0) : 1;
        return getMessageToSend(commandContext.getAuthor(), page)
            .thenCompose(messageToSend -> FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel())))
            .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    private CompletableFuture<MessageToSend> getMessageToSend(Member actorUser, Integer page) {
        AServer server = serverManagementService.loadServer(actorUser.getGuild());
        LeaderBoard leaderBoard = userExperienceService.findLeaderBoardData(server, page);
        List<CompletableFuture> futures = new ArrayList<>();
        CompletableFuture<List<LeaderBoardEntryModel>> completableFutures = converter.fromLeaderBoard(leaderBoard);
        futures.add(completableFutures);
        log.info("Rendering leaderboard for page {} in server {} for user {}.", page, actorUser.getId(), actorUser.getGuild().getId());
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(actorUser);
        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(aUserInAServer);
        CompletableFuture<List<LeaderBoardEntryModel>> userRankFuture = converter.fromLeaderBoardEntry(Arrays.asList(userRank));
        futures.add(userRankFuture);
        return FutureUtils.toSingleFuture(futures).thenCompose(aVoid -> {
            List<LeaderBoardEntryModel> finalModels = completableFutures.join();
            LeaderBoardModel leaderBoardModel = LeaderBoardModel
                    .builder()
                    .userExperiences(finalModels)
                    .userExecuting(userRankFuture.join().get(0))
                    .build();
            return CompletableFuture.completedFuture(templateService.renderEmbedTemplate(LEADER_BOARD_POST_EMBED_TEMPLATE, leaderBoardModel, actorUser.getGuild().getIdLong()));

        });
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Integer page;
        if(slashCommandParameterService.hasCommandOption(PAGE_PARAMETER, event)) {
            page = slashCommandParameterService.getCommandOption(PAGE_PARAMETER, event, Integer.class);
        } else {
            page = 1;
        }
        return getMessageToSend(event.getMember(), page)
                .thenCompose(messageToSend -> interactionService.replyMessageToSend(messageToSend, event))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<ParameterValidator> leaderBoardPageValidators = Arrays.asList(MinIntegerValueValidator.min(0L));
        Parameter pageParameter = Parameter
                .builder()
                .name(PAGE_PARAMETER)
                .validators(leaderBoardPageValidators)
                .optional(true)
                .templated(true)
                .type(Integer.class)
                .build();
        List<Parameter> parameters = Arrays.asList(pageParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE)
                .commandName(LEADERBOARD_PARAMTER)
                .build();


        return CommandConfiguration.builder()
                .name(LEADERBOARD_PARAMTER)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
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
