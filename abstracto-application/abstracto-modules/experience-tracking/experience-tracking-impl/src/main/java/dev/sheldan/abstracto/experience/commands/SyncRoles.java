package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SyncRoles extends AbstractConditionableCommand {

    @Autowired
    private AUserExperienceService userExperienceService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        AServer server = commandContext.getUserInitiatedContext().getServer();
        log.info("Synchronizing roles on server {}", server.getId());
        userExperienceService.syncUserRolesWithFeedback(server, commandContext.getUserInitiatedContext().getChannel());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().longHelp("Syncs the role of the current known users with their respective xp.").usage("syncExpRoles").build();
        return CommandConfiguration.builder()
                .name("syncExpRoles")
                .module(ExperienceModule.EXPERIENCE)
                .description("Syncs the roles of the users with their respective experience.")
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
