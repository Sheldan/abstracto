package dev.sheldan.abstracto.vccontext.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.vccontext.config.VoiceChannelContextFeatureDefinition;
import dev.sheldan.abstracto.vccontext.service.VoiceChannelContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DeleteVoiceChannelContext extends AbstractConditionableCommand {

    @Autowired
    private VoiceChannelContextService voiceChannelContextService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        AChannel channel = (AChannel) parameters.get(0);
        AChannel actualChannel = channelManagementService.loadChannel(channel.getId());
        voiceChannelContextService.deleteVoiceChannelContext(actualChannel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter voiceChannel = Parameter
                .builder()
                .name("channel")
                .type(AChannel.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(voiceChannel);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("deleteVoiceChannelContext")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .messageCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return VoiceChannelContextFeatureDefinition.VOICE_CHANNEL_CONTEXT;
    }
}
