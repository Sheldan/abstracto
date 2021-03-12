package dev.sheldan.abstracto.repostdetection.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionFeatureDefinition;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionModuleDefinition;
import dev.sheldan.abstracto.repostdetection.service.RepostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PurgeReposts extends AbstractConditionableCommand {

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private RepostService repostService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(!parameters.isEmpty()) {
            AUserInAServer fakeUser = (AUserInAServer) parameters.get(0);
            AUserInAServer actualUser = userInServerManagementService.loadOrCreateUser(fakeUser.getUserInServerId());
            repostService.purgeReposts(actualUser);
        } else {
            repostService.purgeReposts(commandContext.getGuild());
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelToSet = Parameter.builder().name("member").type(AUserInAServer.class).templated(true).optional(true).build();
        List<Parameter> parameters = Arrays.asList(channelToSet);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("purgeReposts")
                .module(RepostDetectionModuleDefinition.REPOST_DETECTION)
                .templated(true)
                .async(false)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RepostDetectionFeatureDefinition.REPOST_DETECTION;
    }
}
