package dev.sheldan.abstracto.vccontext.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncVoiceChannelJoinedListener;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.listener.VoiceChannelJoinedModel;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.vccontext.config.VoiceChannelContextFeatureDefinition;
import dev.sheldan.abstracto.vccontext.model.VoiceChannelContext;
import dev.sheldan.abstracto.vccontext.service.management.VoiceChannelContextManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class VoiceChannelContextJoinedListener implements AsyncVoiceChannelJoinedListener {

    @Autowired
    private VoiceChannelContextManagementService voiceChannelContextManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private RoleService roleService;

    @Override
    public DefaultListenerResult execute(VoiceChannelJoinedModel model) {
        log.info("Member {} joined voice channel {}.", model.getMember().getIdLong(), model.getChannel().getIdLong());
        AChannel aChannel = channelManagementService.loadChannel(model.getChannel().getIdLong());
        Optional<VoiceChannelContext> voiceChannelContextOptional = voiceChannelContextManagementService.getVoiceChannelContextForChannel(aChannel);
        if(voiceChannelContextOptional.isPresent()) {
            VoiceChannelContext context = voiceChannelContextOptional.get();
            log.info("Channel {} is a voice channel with context - applying role {}.", aChannel.getId(), context.getRole().getId());
            Long roleId = context.getRole().getId();
            roleService.addRoleToMemberAsync(model.getMember(), context.getRole())
            .thenAccept(unused -> log.info("Successfully applied role {} to member {} for channel {}.", roleId, model.getMember().getId(), model.getChannel().getId()))
            .exceptionally(throwable -> {
                log.error("Failed to apply role {} to member {} for channel {}.", roleId, model.getMember().getId(), model.getChannel().getId(), throwable);
                return null;
            });
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return VoiceChannelContextFeatureDefinition.VOICE_CHANNEL_CONTEXT;
    }
}
