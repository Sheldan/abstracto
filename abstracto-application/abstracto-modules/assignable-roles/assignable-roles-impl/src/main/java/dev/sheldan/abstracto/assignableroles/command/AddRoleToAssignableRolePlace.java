package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceSlashCommandName;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRoleNotUsableException;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlaceManagementService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
public class AddRoleToAssignableRolePlace extends AbstractConditionableCommand {

    private static final String ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER = "name";
    private static final String ASSIGNABLE_ROLE_PARAMETER = "role";
    private static final String DISPLAY_TEXT_PARAMETER = "displayText";
    private static final String EMOTE_PARAMETER = "emote";

    private static final String ADD_ROLE_TO_ASSIGNABLE_ROLE_POST_RESPONSE = "addRoleToAssignableRolePlace_response";
    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private RoleService roleService;

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
        Role role = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PARAMETER, event, Role.class);
        String displayText;
        if(slashCommandParameterService.hasCommandOption(DISPLAY_TEXT_PARAMETER, event)) {
            displayText = slashCommandParameterService.getCommandOption(DISPLAY_TEXT_PARAMETER, event, String.class);
        } else {
            displayText = null;
        }

        Emoji emoji;
        if(slashCommandParameterService.hasCommandOption(EMOTE_PARAMETER, event)) {
            String emoteText = slashCommandParameterService.getCommandOption(EMOTE_PARAMETER, event, String.class);
            emoji = slashCommandParameterService.loadEmoteFromString(emoteText, event.getGuild());
        } else {
            emoji = null;
        }
        AServer server = serverManagementService.loadServer(event.getGuild());
        // already used check via role and assignable role place name
        if(!roleService.canBotInteractWithRole(role)) {
            throw new AssignableRoleNotUsableException(role);
        }
        return service.addRoleToAssignableRolePlace(server, assignableRolePlaceName, role, emoji, displayText)
            .thenAccept(unused -> interactionService.replyEmbed(ADD_ROLE_TO_ASSIGNABLE_ROLE_POST_RESPONSE, event))
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
        Parameter rolePostName = Parameter
                .builder()
                .name(DISPLAY_TEXT_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter emote = Parameter
                .builder()
                .name(EMOTE_PARAMETER)
                .type(AEmote.class)
                .optional(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(placeName, role, rolePostName, emote);
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
            .commandName("add")
            .build();

        return CommandConfiguration.builder()
                .name("addRoleToAssignableRolePlace")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .causesReaction(true)
                .slashCommandConfig(slashCommandConfig)
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
