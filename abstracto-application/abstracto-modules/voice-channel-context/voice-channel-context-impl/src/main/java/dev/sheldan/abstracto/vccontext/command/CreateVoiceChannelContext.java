package dev.sheldan.abstracto.vccontext.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.vccontext.config.VoiceChannelContextFeatureDefinition;
import dev.sheldan.abstracto.vccontext.service.VoiceChannelContextService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CreateVoiceChannelContext extends AbstractConditionableCommand {

    @Autowired
    private VoiceChannelContextService voiceChannelContextService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        VoiceChannel channel = (VoiceChannel) parameters.get(0);
        Role role = (Role) parameters.get(1);
        voiceChannelContextService.createVoiceChannelContext(channel, role);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter voiceChannel = Parameter.builder().name("voiceChannel").type(VoiceChannel.class).templated(true).build();
        Parameter contextRole = Parameter.builder().name("role").type(Role.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(voiceChannel, contextRole);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("createVoiceChannelContext")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
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
