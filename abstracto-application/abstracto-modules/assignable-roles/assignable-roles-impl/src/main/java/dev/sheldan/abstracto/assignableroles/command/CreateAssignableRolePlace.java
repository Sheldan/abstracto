package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceSlashCommandName;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlaceType;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MaxStringLengthValidator;
import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CreateAssignableRolePlace extends AbstractConditionableCommand {

    private static final String ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER = "name";
    private static final String ASSIGNABLE_ROLE_PLACE_CHANNEL_PARAMETER = "channel";
    private static final String ASSIGNABLE_ROLE_PLACE_TYPE_PARAMETER = "type";
    private static final String ASSIGNABLE_ROLE_PLACE_TEXT_PARAMETER = "text";
    private static final String CREATE_ASSIGNABLE_ROLE_PLACE_RESPONSE_TEMPLATE = "createAssignableRolePlace_response";
    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String assignableRolePlaceName = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER, event, String.class);
        GuildChannel channel = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PLACE_CHANNEL_PARAMETER, event, TextChannel.class, GuildChannel.class);
        if(!channel.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        String text = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PLACE_TEXT_PARAMETER, event, String.class);
        AssignableRolePlaceType type;
        if(slashCommandParameterService.hasCommandOption(ASSIGNABLE_ROLE_PLACE_TYPE_PARAMETER, event)) {
            String typeString = slashCommandParameterService.getCommandOption(ASSIGNABLE_ROLE_PLACE_TYPE_PARAMETER, event, String.class);
            type = CommandParameterKey.getEnumFromKey(AssignableRolePlaceType.class, typeString);
        } else {
            type = AssignableRolePlaceType.DEFAULT;
        }
        AChannel chosenChannel = channelManagementService.loadChannel(channel.getIdLong());
        service.createAssignableRolePlace(assignableRolePlaceName, chosenChannel, text, type);
        return interactionService.replyEmbed(CREATE_ASSIGNABLE_ROLE_PLACE_RESPONSE_TEMPLATE, event)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<ParameterValidator> rolePlaceNameValidator = Arrays.asList(MaxStringLengthValidator.max(AssignableRolePlace.ASSIGNABLE_PLACE_NAME_LIMIT));
        Parameter rolePostName = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PLACE_NAME_PARAMETER)
                .validators(rolePlaceNameValidator)
                .type(String.class)
                .templated(true)
                .build();
        Parameter channel = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PLACE_CHANNEL_PARAMETER)
                .type(TextChannel.class)
                .templated(true)
                .build();
        Parameter type = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PLACE_TYPE_PARAMETER)
                .type(AssignableRolePlaceType.class)
                .templated(true)
                .optional(true)
                .build();
        List<ParameterValidator> rolePlaceDescriptionValidator = Arrays.asList(MaxStringLengthValidator.max(AssignableRolePlace.ASSIGNABLE_PLACE_NAME_LIMIT));
        Parameter text = Parameter
                .builder()
                .name(ASSIGNABLE_ROLE_PLACE_TEXT_PARAMETER)
                .validators(rolePlaceDescriptionValidator)
                .type(String.class)
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .rootCommandName(AssignableRolePlaceSlashCommandName.ASSIGNABLE_ROLE_PLACE)
            .groupName("place")
            .commandName("create")
            .build();

        List<String> aliases = Arrays.asList("crRPl", "crAssRoPl");
        List<Parameter> parameters = Arrays.asList(rolePostName, channel, text, type);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("createAssignableRolePlace")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .slashCommandConfig(slashCommandConfig)
                .slashCommandOnly(true)
                .parameters(parameters)
                .aliases(aliases)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }
}
