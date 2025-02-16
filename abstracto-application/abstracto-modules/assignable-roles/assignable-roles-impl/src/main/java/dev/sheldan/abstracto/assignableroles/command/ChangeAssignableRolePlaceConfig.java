package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceSlashCommandName;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to change one attribute of an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
 */
@Component
@Slf4j
public class ChangeAssignableRolePlaceConfig extends AbstractConditionableCommand {

    private static final String ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER = "name";
    private static final String CONFIGURATION_KEY_PARAMETER = "key";
    private static final String CONFIGURATION_VALUE_PARAMETER = "value";
    private static final String CHANGE_ASSIGNABLE_ROLE_PLACE_CONFIG_RESPONSE = "changeAssignableRolePlaceConfig_response";
    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private AssignableRolePlaceManagementService assignableRolePlaceManagementService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String assignableRolePlaceName = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER, event, String.class);
        String assignableRolePlaceParameterKeyString = slashCommandParameterService.getCommandOption(CONFIGURATION_KEY_PARAMETER, event, String.class);
        AssignableRolePlaceParameterKey enumFromKey =
            CommandParameterKey.getEnumFromKey(AssignableRolePlaceParameterKey.class, assignableRolePlaceParameterKeyString);
        String parameterValue = slashCommandParameterService.getCommandOption(CONFIGURATION_VALUE_PARAMETER, event, String.class);
        AServer server = serverManagementService.loadServer(event.getGuild());
        return service.changeConfiguration(server, assignableRolePlaceName, enumFromKey, parameterValue)
            .thenAccept(unused -> interactionService.replyEmbed(CHANGE_ASSIGNABLE_ROLE_PLACE_CONFIG_RESPONSE, event))
            .thenApply(aVoid -> CommandResult.fromSuccess());
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
        Parameter assignableRolePlaceName = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();
        Parameter parameterKey = Parameter
                .builder()
                .name(CONFIGURATION_KEY_PARAMETER)
                .type(AssignableRolePlaceParameterKey.class)
                .templated(true)
                .build();
        Parameter parameterValue = Parameter
                .builder()
                .name(CONFIGURATION_VALUE_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();


        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .rootCommandName(AssignableRolePlaceSlashCommandName.ASSIGNABLE_ROLE_PLACE)
            .groupName("place")
            .commandName("changeconfig")
            .build();

        List<Parameter> parameters = Arrays.asList(assignableRolePlaceName, parameterKey, parameterValue);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();
        return CommandConfiguration.builder()
                .name("changeAssignableRolePlaceConfig")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .slashCommandOnly(true)
                .async(true)
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
