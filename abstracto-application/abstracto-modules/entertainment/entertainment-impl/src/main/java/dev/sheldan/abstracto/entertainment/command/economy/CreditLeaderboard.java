package dev.sheldan.abstracto.entertainment.command.economy;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.CreditsLeaderboardEntry;
import dev.sheldan.abstracto.entertainment.model.command.CreditsLeaderboardResponseModel;
import dev.sheldan.abstracto.entertainment.service.EconomyService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class CreditLeaderboard extends AbstractConditionableCommand {

    private static final String CREDIT_LEADERBOARD_COMMAND_NAME = "creditLeaderboard";
    private static final String CREDIT_LEADERBOARD_RESPONSE = "creditLeaderboard_response";
    private static final String PAGE_PARAMETER = "page";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private EconomyService economyService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private MemberService memberService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Integer page;
        if(slashCommandParameterService.hasCommandOption(PAGE_PARAMETER, event)) {
            page = slashCommandParameterService.getCommandOption(PAGE_PARAMETER, event, Integer.class);
        } else {
            page = 1;
        }
        AServer server = serverManagementService.loadServer(event.getGuild());
        List<CreditsLeaderboardEntry> creditLeaderboard = economyService.getCreditLeaderboard(server, page);
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(event.getMember());
        CreditsLeaderboardEntry ownRank = economyService.getRankOfUser(aUserInAServer);
        CreditsLeaderboardResponseModel model = CreditsLeaderboardResponseModel
                .builder()
                .entries(creditLeaderboard)
                .ownRank(ownRank)
                .build();
        return enrichModelWithMembers(model, event.getGuild().getIdLong())
                .thenCompose(model1 -> FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(CREDIT_LEADERBOARD_RESPONSE, model1, event.getHook())))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    private CompletableFuture<CreditsLeaderboardResponseModel> enrichModelWithMembers(CreditsLeaderboardResponseModel model, Long serverId) {
        List<CompletableFuture<Member>> memberFutures = new ArrayList<>();
        model.getEntries().forEach(creditsLeaderboardEntry -> {
            memberFutures.add(memberService.getMemberInServerAsync(serverId, creditsLeaderboardEntry.getMemberDisplay().getUserId()));
        });
        memberFutures.add(memberService.getMemberInServerAsync(serverId, model.getOwnRank().getMemberDisplay().getUserId()));
        CompletableFuture<CreditsLeaderboardResponseModel> modelFuture = new CompletableFuture<>();
        CompletableFutureList<Member> futureList = new CompletableFutureList<>(memberFutures);
        futureList.getMainFuture().whenComplete((unused, throwable) -> {
            Map<Long, Member> memberMap = new HashMap<>();
            futureList.getObjects().forEach(member -> memberMap.put(member.getIdLong(), member));
            model.getEntries().forEach(creditsLeaderboardEntry -> {
                if(memberMap.containsKey(creditsLeaderboardEntry.getMemberDisplay().getUserId())) {
                    creditsLeaderboardEntry.setMember(memberMap.get(creditsLeaderboardEntry.getMemberDisplay().getUserId()));
                }
            });
            if(memberMap.containsKey(model.getOwnRank().getMemberDisplay().getUserId())) {
                model.getOwnRank().setMember(memberMap.get(model.getOwnRank().getMemberDisplay().getUserId()));
            }
            modelFuture.complete(model);
        });
        return modelFuture;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter pageParameter = Parameter
                .builder()
                .name(PAGE_PARAMETER)
                .optional(true)
                .templated(true)
                .type(Integer.class)
                .build();
        List<Parameter> parameters = Arrays.asList(pageParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(EntertainmentSlashCommandNames.ECONOMY)
                .commandName("creditboard")
                .build();


        return CommandConfiguration.builder()
                .name(CREDIT_LEADERBOARD_COMMAND_NAME)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ECONOMY;
    }
}
