package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.exception.EmoteNotInAssignableRolePlaceException;
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
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Command used to swap the positions of two {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRole roles}
 * within one {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
 */
@Component
public class SwapAssignableRolePosition extends AbstractConditionableCommand {

    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String name = (String) parameters.get(0);
        FullEmote firstEmote = (FullEmote) parameters.get(1);
        FullEmote secondEmote = (FullEmote) parameters.get(2);
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        if(emoteService.compareAEmote(firstEmote.getFakeEmote(), secondEmote.getFakeEmote())) {
            return CommandResult.fromError("You cannot swap the same emote");
        }
        if(!service.hasAssignableRolePlaceEmote(server, name, firstEmote.getFakeEmote())) {
            throw new EmoteNotInAssignableRolePlaceException(firstEmote, name);
        }
        if(!service.hasAssignableRolePlaceEmote(server, name, firstEmote.getFakeEmote())) {
            throw new EmoteNotInAssignableRolePlaceException(secondEmote, name);
        }
        service.swapPositions(server, name, firstEmote, secondEmote);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter rolePostName = Parameter.builder().name("name").type(String.class).templated(true).build();
        Parameter firstEmote = Parameter.builder().name("firstEmote").type(FullEmote.class).templated(true).build();
        Parameter secondEmote = Parameter.builder().name("secondEmote").type(FullEmote.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(rolePostName, firstEmote, secondEmote);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("swapAssignableRolePosition")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .causesReaction(true)
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
