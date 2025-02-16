package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceSlashCommandName;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlaceManagementService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to delete an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
 */
@Component
public class DeleteAssignableRolePlace extends AbstractConditionableCommand {

    private static final String ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER = "name";
    private static final String DELETE_ASSIGNABLE_ROLE_PLACE_RESPONSE_TEMPLATE = "deleteAssignableRolePlace_response";

    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SlashCommandService slashCommandService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private AssignableRolePlaceManagementService assignableRolePlaceManagementService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String assignableRolePlaceName = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER, event, String.class);
        AServer server = serverManagementService.loadServer(event.getGuild());
        return service.deleteAssignableRolePlace(server, assignableRolePlaceName)
            .thenAccept(unused -> slashCommandService.completeConfirmableCommand(event, DELETE_ASSIGNABLE_ROLE_PLACE_RESPONSE_TEMPLATE))
            .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER)) {
            String input = event.getFocusedOption().getValue();
            AServer server = serverManagementService.loadServer(event.getGuild());
            return assignableRolePlaceManagementService.getAssignableRolePlacesWithNamesContaining(input, server)
                .stream().map(assignableRolePlace -> assignableRolePlace.getKey().toLowerCase())
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter rolePostName = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(rolePostName);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .rootCommandName(AssignableRolePlaceSlashCommandName.ASSIGNABLE_ROLE_PLACE)
            .groupName("place")
            .commandName("delete")
            .build();

        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();
        return CommandConfiguration.builder()
                .name("deleteAssignableRolePlace")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .causesReaction(true)
                .async(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .requiresConfirmation(true)
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
