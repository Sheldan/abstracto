package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to synchronize the actual awarded roles which what is defined to be awarded in the database.
 * This also calculates the appropriate role for each user and then awards the role, while removing the previously awarded role.
 * The synchronization is usually a longer process, and there is a status message to see how far this progress is.
 *
 */
@Component
@Slf4j
public class SyncRoles extends AbstractConditionableCommand {

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        AServer server =  serverManagementService.loadServer(commandContext.getGuild());
        log.info("Synchronizing roles on server {}", server.getId());
        return userExperienceService.syncUserRolesWithFeedback(server, commandContext.getChannel())
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("syncExpRoles")
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .async(true)
                .requiresConfirmation(true)
                .supportsEmbedException(true)
                .messageCommandOnly(true)
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
