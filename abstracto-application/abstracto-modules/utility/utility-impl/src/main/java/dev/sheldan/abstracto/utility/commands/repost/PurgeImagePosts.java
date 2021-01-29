package dev.sheldan.abstracto.utility.commands.repost;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.utility.config.RepostDetectionModuleInterface;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.service.PostedImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PurgeImagePosts extends AbstractConditionableCommand {

    @Autowired
    private PostedImageService postedImageService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(!parameters.isEmpty()) {
            AUserInAServer fakeUser = (AUserInAServer) parameters.get(0);
            AUserInAServer actualUser = userInServerManagementService.loadOrCreateUser(fakeUser.getUserInServerId());
            postedImageService.purgePostedImages(actualUser);
        } else {
            postedImageService.purgePostedImages(commandContext.getGuild());
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelToSet = Parameter.builder().name("member").type(AUserInAServer.class).templated(true).optional(true).build();
        List<Parameter> parameters = Arrays.asList(channelToSet);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("purgeImagePosts")
                .module(RepostDetectionModuleInterface.REPOST_DETECTION)
                .templated(true)
                .async(false)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REPOST_DETECTION;
    }
}
