package dev.sheldan.abstracto.core.commands.config.cooldown;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandCoolDownService;
import dev.sheldan.abstracto.core.command.service.CommandCoolDownServiceBean;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class CommandCoolDownChannelGroup extends AbstractConditionableCommand {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private CommandCoolDownService coolDownService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        AChannelGroup fakeChannelGroup = (AChannelGroup) parameters.get(0);
        Duration channelDuration = (Duration) parameters.get(1);
        Duration memberDuration = (Duration) parameters.get(2);
        AServer actualServer = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
        AChannelGroup actualChannelGroup = channelGroupManagementService.findByNameAndServerAndType(
                fakeChannelGroup.getGroupName(), actualServer, CommandCoolDownServiceBean.COOL_DOWN_CHANNEL_GROUP_TYPE);
        coolDownService.setCoolDownConfigForChannelGroup(actualChannelGroup, channelDuration, memberDuration);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("channelGroupName").templated(true).type(AChannelGroup.class).build();
        Parameter channelDuration = Parameter.builder().name("channelDuration").templated(true).type(Duration.class).build();
        Parameter memberDuration = Parameter.builder().name("memberDuration").templated(true).type(Duration.class).build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelDuration, memberDuration);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("commandCoolDownChannelGroup")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
