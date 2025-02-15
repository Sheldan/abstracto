package dev.sheldan.abstracto.entertainment.command.economy;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.CreditsLeaderboardEntry;
import dev.sheldan.abstracto.entertainment.model.command.CreditsModel;
import dev.sheldan.abstracto.entertainment.service.EconomyService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Credits extends AbstractConditionableCommand {

    private static final String CREDITS_COMMAND = "credits";
    private static final String CREDITS_RESPONSE = "credits_response";

    @Autowired
    private EconomyService economyService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        AUserInAServer targetUser = userInServerManagementService.loadOrCreateUser(event.getMember());
        CreditsLeaderboardEntry rankEntry = economyService.getRankOfUser(targetUser);
        rankEntry.setMember(event.getMember());
        CreditsModel model = CreditsModel
                .builder()
                .entry(rankEntry)
                .build();
        return interactionService.replyEmbed(CREDITS_RESPONSE, model, event)
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
                .commandName("credits")
                .build();

        return CommandConfiguration.builder()
                .name(CREDITS_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .slashCommandOnly(true)
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
