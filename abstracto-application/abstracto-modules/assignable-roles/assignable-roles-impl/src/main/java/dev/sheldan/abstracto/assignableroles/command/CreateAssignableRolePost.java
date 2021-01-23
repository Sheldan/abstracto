package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.features.AssignableRoleFeature;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MaxStringLengthValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CreateAssignableRolePost extends AbstractConditionableCommand {

    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String name = (String) parameters.get(0);
        MessageChannel channel = (TextChannel) parameters.get(1);
        String text =  (String) parameters.get(2);
        AChannel chosenChannel = channelManagementService.loadChannel(channel.getIdLong());
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        service.createAssignableRolePlace(server, name, chosenChannel, text);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<ParameterValidator> rolePlaceNameValidator = Arrays.asList(MaxStringLengthValidator.max(AssignableRolePlace.ASSIGNABLE_PLACE_NAME_LIMIT));
        Parameter rolePostName = Parameter.builder().name("name").validators(rolePlaceNameValidator).type(String.class).templated(true).build();
        Parameter channel = Parameter.builder().name("channel").type(TextChannel.class).templated(true).build();
        List<ParameterValidator> rolePlaceDescriptionValidator = Arrays.asList(MaxStringLengthValidator.max(AssignableRolePlace.ASSIGNABLE_PLACE_NAME_LIMIT));
        Parameter text = Parameter.builder().name("text").validators(rolePlaceDescriptionValidator).type(String.class).remainder(true).optional(true).templated(true).build();
        List<String> aliases = Arrays.asList("crRPl", "crAssRoPl");
        List<Parameter> parameters = Arrays.asList(rolePostName, channel, text);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("createAssignableRolePlace")
                .module(AssignableRoleModule.ASSIGNABLE_ROLES)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .aliases(aliases)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return AssignableRoleFeature.ASSIGNABLE_ROLES;
    }
}
