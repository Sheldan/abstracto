package dev.sheldan.abstracto.entertainment.command.economy;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.dto.PayDayResult;
import dev.sheldan.abstracto.entertainment.model.command.PayDayResponseModel;
import dev.sheldan.abstracto.entertainment.model.database.EconomyLeaderboardResult;
import dev.sheldan.abstracto.entertainment.service.EconomyService;
import dev.sheldan.abstracto.entertainment.service.management.EconomyUserManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Payday extends AbstractConditionableCommand {

    private static final String PAYDAY_COMMAND_NAME = "payday";

    @Autowired
    private EconomyService economyService;

    @Autowired
    private EconomyUserManagementService economyUserManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    private static final String PAYDAY_RESPONSE = "payday_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Member member = commandContext.getAuthor();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        PayDayResult payDayResult = economyService.triggerPayDay(aUserInAServer);
        EconomyLeaderboardResult rank = economyUserManagementService.getRankOfUserInServer(aUserInAServer);
        PayDayResponseModel responseModel = PayDayResponseModel
                .builder()
                .currentCredits(payDayResult.getCurrentCredits())
                .gainedCredits(payDayResult.getGainedCredits())
                .leaderboardPosition(rank.getRank().longValue())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(PAYDAY_RESPONSE, responseModel, member.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        PayDayResult payDayResult = economyService.triggerPayDay(aUserInAServer);
        EconomyLeaderboardResult rank = economyUserManagementService.getRankOfUserInServer(aUserInAServer);
        PayDayResponseModel responseModel = PayDayResponseModel
                .builder()
                .currentCredits(payDayResult.getCurrentCredits())
                .gainedCredits(payDayResult.getGainedCredits())
                .leaderboardPosition(rank.getRank().longValue())
                .build();
        return interactionService.replyEmbed(PAYDAY_RESPONSE, responseModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(EntertainmentSlashCommandNames.ECONOMY)
                .commandName("payday")
                .build();

        return CommandConfiguration.builder()
                .name(PAYDAY_COMMAND_NAME)
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
        return EntertainmentFeatureDefinition.ECONOMY;
    }
}
