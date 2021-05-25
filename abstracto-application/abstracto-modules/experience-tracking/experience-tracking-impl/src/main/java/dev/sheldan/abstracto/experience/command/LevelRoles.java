package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.model.template.LevelRole;
import dev.sheldan.abstracto.experience.model.template.LevelRolesModel;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class LevelRoles extends AbstractConditionableCommand {

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    private static final String LEVEL_ROLES_TEMPLATE_KEY = "levelRoles_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        List<LevelRole> levelRoles = experienceRoleService.loadLevelRoleConfigForServer(server);
        levelRoles = levelRoles.stream().sorted(Comparator.comparingInt(LevelRole::getLevel).reversed()).collect(Collectors.toList());
        LevelRolesModel model = LevelRolesModel
                .builder()
                .levelRoles(levelRoles)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(LEVEL_ROLES_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("levelRoles")
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .async(true)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
