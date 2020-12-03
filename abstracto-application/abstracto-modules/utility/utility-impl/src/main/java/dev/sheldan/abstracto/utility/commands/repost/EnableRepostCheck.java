package dev.sheldan.abstracto.utility.commands.repost;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.utility.config.RepostDetectionModuleInterface;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import dev.sheldan.abstracto.utility.service.RepostServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EnableRepostCheck extends AbstractConditionableCommand {

    @Autowired
    private RepostCheckChannelService repostCheckChannelService;

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        AChannelGroup fakeChannelGroup = (AChannelGroup) parameters.get(0);
        AServer actualServer = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
        AChannelGroup actualChannelGroup = channelGroupManagementService.findByNameAndServerAndType(fakeChannelGroup.getGroupName(), actualServer, RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE);
        repostCheckChannelService.setRepostCheckEnabledForChannelGroup(actualChannelGroup);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelToSet = Parameter.builder().name("channelGroup").type(AChannelGroup.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(channelToSet);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("enableRepostCheck")
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
