package dev.sheldan.abstracto.entertainment.command.games;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.exception.NotEnoughCreditsException;
import dev.sheldan.abstracto.entertainment.model.command.games.MineBoard;
import dev.sheldan.abstracto.entertainment.model.database.EconomyUser;
import dev.sheldan.abstracto.entertainment.service.GameService;
import dev.sheldan.abstracto.entertainment.service.management.EconomyUserManagementService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class Mines extends AbstractConditionableCommand {

    private static final String MINES_COMMAND_NAME = "mines";
    private static final String WIDTH_PARAMETER = "width";
    private static final String HEIGHT_PARAMETER = "height";
    private static final String MINES_PARAMETER = "mines";
    private static final String CREDITS_PARAMETER = "credits";
    public static final String MINE_BOARD_TEMPLATE_KEY = "mines_board_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private EconomyUserManagementService economyUserManagementService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Integer width = 5;
        if(slashCommandParameterService.hasCommandOption(WIDTH_PARAMETER, event)) {
            width = slashCommandParameterService.getCommandOption(WIDTH_PARAMETER, event, Integer.class);
        }
        Integer height = 5;
        if(slashCommandParameterService.hasCommandOption(HEIGHT_PARAMETER, event)) {
            height = slashCommandParameterService.getCommandOption(HEIGHT_PARAMETER, event, Integer.class);
        }
        Integer mines = 5;
        if(slashCommandParameterService.hasCommandOption(MINES_PARAMETER, event)) {
            mines = slashCommandParameterService.getCommandOption(MINES_PARAMETER, event, Integer.class);
        }
        Integer credit = null;
        long serverId = event.getGuild().getIdLong();
        boolean economyEnabled = featureFlagService.getFeatureFlagValue(EntertainmentFeatureDefinition.ECONOMY, serverId);
        if(economyEnabled){
            credit = 50;
            if(slashCommandParameterService.hasCommandOption(CREDITS_PARAMETER, event)) {
                credit = slashCommandParameterService.getCommandOption(CREDITS_PARAMETER, event, Integer.class);
            }

            Optional<EconomyUser> userOptional = economyUserManagementService.getUser(ServerUser.fromMember(event.getMember()));
            if(!userOptional.isPresent()) {
                throw new NotEnoughCreditsException();
            }
            EconomyUser user = userOptional.get();
            if(user.getCredits() < credit) {
                throw new NotEnoughCreditsException();
            }
        }
        MineBoard board = gameService.createBoard(width, height, mines, serverId);
        board.setCreditsEnabled(economyEnabled);
        board.setUserId(event.getMember().getIdLong());
        board.setServerId(serverId);
        board.setCredits(credit);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MINE_BOARD_TEMPLATE_KEY, board, serverId);
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenCompose(interactionHook -> interactionHook.retrieveOriginal().submit())
                .thenApply(message -> {
                    gameService.persistMineBoardMessage(board, message);
                    return CommandResult.fromSuccess();
                });
    }


    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Integer width = 5;
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(!parameters.isEmpty()) {
            width = (Integer) parameters.get(0);
        }
        Integer height = 5;
        if(parameters.size() >= 2) {
            height = (Integer) parameters.get(1);
        }
        Integer mines = 5;
        if(parameters.size() >= 3) {
            mines = (Integer) parameters.get(2);
        }
        Integer credit = null;
        long serverId = commandContext.getGuild().getIdLong();
        boolean economyEnabled = featureFlagService.getFeatureFlagValue(EntertainmentFeatureDefinition.ECONOMY, serverId);
        if(economyEnabled){
            credit = 50;
            if(parameters.size() == 4) {
                credit = (Integer) parameters.get(3);
            }

            Optional<EconomyUser> userOptional = economyUserManagementService.getUser(ServerUser.fromMember(commandContext.getAuthor()));
            if(!userOptional.isPresent()) {
                throw new NotEnoughCreditsException();
            }
            EconomyUser user = userOptional.get();
            if(user.getCredits() < credit) {
                throw new NotEnoughCreditsException();
            }
        }
        MineBoard board = gameService.createBoard(width, height, mines, serverId);
        board.setCreditsEnabled(economyEnabled);
        board.setUserId(commandContext.getAuthor().getIdLong());
        board.setServerId(serverId);
        board.setCredits(credit);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MINE_BOARD_TEMPLATE_KEY, board, serverId);
        List<CompletableFuture<Message>> futures = channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel());
        return FutureUtils.toSingleFutureGeneric(futures)
                .thenAccept(unused ->  gameService.persistMineBoardMessage(board, futures.get(0).join()))
                .thenApply(unused -> CommandResult.fromSuccess());

    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter widthParameter = Parameter
                .builder()
                .name(WIDTH_PARAMETER)
                .type(Integer.class)
                .optional(true)
                .templated(true)
                .build();
        parameters.add(widthParameter);

        Parameter heightParameter = Parameter
                .builder()
                .name(HEIGHT_PARAMETER)
                .type(Integer.class)
                .optional(true)
                .templated(true)
                .build();
        parameters.add(heightParameter);

        Parameter minesParameter = Parameter
                .builder()
                .name(MINES_PARAMETER)
                .type(Integer.class)
                .optional(true)
                .templated(true)
                .build();
        parameters.add(minesParameter);

        Parameter creditsParameter = Parameter
                .builder()
                .name(CREDITS_PARAMETER)
                .type(Integer.class)
                .optional(true)
                .templated(true)
                .dependentFeatures(Arrays.asList(EntertainmentFeatureDefinition.ECONOMY.getKey()))
                .build();
        parameters.add(creditsParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(EntertainmentSlashCommandNames.GAME)
                .commandName(MINES_COMMAND_NAME)
                .build();

        return CommandConfiguration.builder()
                .name(MINES_COMMAND_NAME)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.GAMES;
    }
}
