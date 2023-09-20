package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to move an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
 * to another {@link dev.sheldan.abstracto.core.models.database.AChannel channel}
 */
@Component
public class MoveAssignableRolePlace extends AbstractConditionableCommand {

    @Autowired
    private AssignableRolePlaceService placeManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String name = (String) parameters.get(0);
        TextChannel newChannel = (TextChannel) parameters.get(1);
        if(!newChannel.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        return placeManagementService.moveAssignableRolePlace(server, name, newChannel)
        .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter rolePostName = Parameter
                .builder()
                .name("name")
                .type(String.class)
                .templated(true)
                .build();
        Parameter channel = Parameter
                .builder()
                .name("channel")
                .type(TextChannel.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(rolePostName, channel);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("moveAssignableRolePlace")
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
