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
import dev.sheldan.abstracto.entertainment.dto.CreditGambleResult;
import dev.sheldan.abstracto.entertainment.model.command.CreditGambleResultModel;
import dev.sheldan.abstracto.entertainment.service.EconomyService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CreditGamble extends AbstractConditionableCommand {
    private static final String CREDIT_GAMBLE_COMMAND = "creditGamble";
    private static final String CREDIT_GAMBLE_RESPONSE = "creditGamble_response";

    @Autowired
    private EconomyService economyService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(event.getMember());
        CreditGambleResult result = economyService.triggerCreditGamble(aUserInAServer);
        CreditGambleResultModel model = CreditGambleResultModel.fromCreditGambleResult(result);
        return interactionService.replyEmbed(CREDIT_GAMBLE_RESPONSE, model, event)
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
                .commandName("creditgamble")
                .build();

        return CommandConfiguration.builder()
                .name(CREDIT_GAMBLE_COMMAND)
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
