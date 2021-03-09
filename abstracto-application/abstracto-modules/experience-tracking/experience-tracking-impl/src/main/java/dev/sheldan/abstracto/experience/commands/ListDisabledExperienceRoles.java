package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.models.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.models.templates.DisabledExperienceRolesModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Creates an embed containing the roles for which the experience gain has been disabled.
 */
@Component
public class ListDisabledExperienceRoles extends AbstractConditionableCommand {

    @Autowired
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        AServer server =  serverManagementService.loadServer(commandContext.getGuild());
        List<ADisabledExpRole> disabledRolesForServer = disabledExpRoleManagementService.getDisabledRolesForServer(server);
        DisabledExperienceRolesModel disabledExperienceRolesModel = (DisabledExperienceRolesModel) ContextConverter.fromCommandContext(commandContext, DisabledExperienceRolesModel.class);
        disabledRolesForServer.forEach(aDisabledExpRole -> {
            Role jdaRole = null;
            if(!aDisabledExpRole.getRole().getDeleted()) {
                jdaRole = roleService.getRoleFromGuild(aDisabledExpRole.getRole());
            }
            FullRole role = FullRole
                    .builder()
                    .role(aDisabledExpRole.getRole())
                    .serverRole(jdaRole)
                    .build();
            disabledExperienceRolesModel.getRoles().add(role);
        });
        List<CompletableFuture<Message>> futures = channelService.sendEmbedTemplateInTextChannelList("list_disabled_experience_roles", disabledExperienceRolesModel, commandContext.getChannel());
        return FutureUtils.toSingleFutureGeneric(futures).thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("lsDisEpRoles");
        return CommandConfiguration.builder()
                .name("listDisabledExperienceRoles")
                .module(ExperienceModule.EXPERIENCE)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .async(true)
                .aliases(aliases)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }
}
