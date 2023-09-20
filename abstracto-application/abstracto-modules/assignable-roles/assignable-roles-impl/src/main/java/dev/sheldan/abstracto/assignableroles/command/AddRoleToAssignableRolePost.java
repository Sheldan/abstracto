package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRoleNotUsableException;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to add an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRole assignableRole}
 * to an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
 */
@Component
public class AddRoleToAssignableRolePost extends AbstractConditionableCommand {

    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String name = (String) parameters.get(0);
        Role role = (Role) parameters.get(1);
        String description = null;
        if (parameters.size() > 2) {
            description = (String) parameters.get(2);
        }
        FullEmote emote = null;
        if(parameters.size() > 3) {
            emote = (FullEmote) parameters.get(3);
        }
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        // already used check via role and assignable role place name
        if(!roleService.canBotInteractWithRole(role)) {
            throw new AssignableRoleNotUsableException(role);
        }
        return service.addRoleToAssignableRolePlace(server, name, role, emote, description)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter placeName = Parameter
                .builder()
                .name("name")
                .type(String.class)
                .templated(true)
                .build();
        Parameter role = Parameter
                .builder()
                .name("role")
                .type(Role.class)
                .templated(true)
                .build();
        Parameter rolePostName = Parameter
                .builder()
                .name("displayText")
                .type(String.class)
                .templated(true)
                .build();
        Parameter emote = Parameter
                .builder()
                .name("emote")
                .type(FullEmote.class)
                .optional(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(placeName, role, rolePostName, emote);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("addRoleToAssignableRolePlace")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .causesReaction(true)
                .messageCommandOnly(true)
                .async(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }
}
