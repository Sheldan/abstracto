package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceSlashCommandName;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleConditionService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlaceManagementService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RemoveAssignableRoleCondition extends AbstractConditionableCommand {

    private static final String ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER = "name";
    private static final String ASSIGNABLE_ROLE_PARAMETER = "role";
    private static final String CONDITION_KEY_PARAMETER = "conditionKey";

    private static final String REMOVE_ASSIGNABLE_ROLE_CONDITION_RESPONSE_TEMPLATE = "removeAssignableRoleCondition_response";

    @Autowired
    private AssignableRoleConditionService assignableRoleConditionService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private AssignableRolePlaceManagementService assignableRolePlaceManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String assignableRolePlaceName = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER, event, String.class);
        Role assignableRole = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PARAMETER, event, Role.class);
        String conditionKeyString = slashCommandParameterService.getCommandOption(CONDITION_KEY_PARAMETER, event, String.class);

        AssignableRoleConditionType assignableRoleConditionType = CommandParameterKey.getEnumFromKey(AssignableRoleConditionType.class, conditionKeyString);
        assignableRoleConditionService.deleteAssignableRoleCondition(assignableRolePlaceName, assignableRole, assignableRoleConditionType);
        return interactionService.replyEmbed(REMOVE_ASSIGNABLE_ROLE_CONDITION_RESPONSE_TEMPLATE, event)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
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
        Parameter placeName = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();
        Parameter role = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PARAMETER)
                .type(Role.class)
                .templated(true)
                .build();
        Parameter conditionKey = Parameter
                .builder()
                .name(CONDITION_KEY_PARAMETER)
                .type(AssignableRoleConditionType.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(placeName, role, conditionKey);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .rootCommandName(AssignableRolePlaceSlashCommandName.ASSIGNABLE_ROLE_PLACE)
            .groupName("role")
            .commandName("removecondition")
            .build();

        return CommandConfiguration.builder()
                .name("removeAssignableRoleCondition")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
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
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }
}
