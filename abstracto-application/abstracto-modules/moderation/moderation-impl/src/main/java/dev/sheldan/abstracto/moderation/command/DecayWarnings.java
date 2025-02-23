package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.WarnService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DecayWarnings extends AbstractConditionableCommand {

    private static final String DECAY_WARNINGS_COMMAND = "decayWarnings";
    private static final String DECAY_WARNINGS_RESPONSE = "decayWarnings_response";

    @Autowired
    private WarnService warnService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private DecayWarnings self;

    @Autowired
    private SlashCommandService slashCommandService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return self.decayAllWarnings(event)
            .thenCompose(hook -> slashCommandService.completeConfirmableCommand(event, DECAY_WARNINGS_RESPONSE))
            .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Transactional
    public CompletableFuture<Void> decayAllWarnings(SlashCommandInteractionEvent event) {
        AServer server = serverManagementService.loadServer(event.getGuild());
        return warnService.decayWarningsForServer(server);
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
            .rootCommandName(ModerationSlashCommandNames.WARN_DECAY)
            .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
            .commandName("decay")
            .build();

        return CommandConfiguration.builder()
                .name(DECAY_WARNINGS_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .requiresConfirmation(true)
                .async(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.AUTOMATIC_WARN_DECAY;
    }
}
