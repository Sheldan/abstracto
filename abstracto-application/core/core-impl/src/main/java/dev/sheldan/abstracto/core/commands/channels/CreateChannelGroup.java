package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.management.ChannelGroupTypeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CreateChannelGroup extends AbstractConditionableCommand {

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private ChannelGroupTypeManagementService channelGroupTypeManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String groupName = (String) parameters.get(0);
        ChannelGroupType fakeChannelGroupType = (ChannelGroupType) parameters.get(1);
        ChannelGroupType actualChannelGroupType = channelGroupTypeManagementService.findChannelGroupTypeByKey(fakeChannelGroupType.getGroupTypeKey());
        channelGroupService.createChannelGroup(groupName, commandContext.getGuild().getIdLong(), actualChannelGroupType);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("name").type(String.class).templated(true).build();
        Parameter channelGroupType = Parameter.builder().name("groupType").type(ChannelGroupType.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelGroupType);
        List<String> aliases = Arrays.asList("+ChGroup");
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("createChannelGroup")
                .module(ChannelsModuleInterface.CHANNELS)
                .parameters(parameters)
                .aliases(aliases)
                .supportsEmbedException(true)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
