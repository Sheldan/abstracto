package dev.sheldan.abstracto.vccontext.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.vccontext.exception.VoiceChannelContextAlreadyExistsException;
import dev.sheldan.abstracto.vccontext.exception.VoiceChannelContextNotExistsException;
import dev.sheldan.abstracto.vccontext.model.VoiceChannelContext;
import dev.sheldan.abstracto.vccontext.service.management.VoiceChannelContextManagementService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VoiceChannelContextServiceBean implements VoiceChannelContextService {

    @Autowired
    private VoiceChannelContextManagementService voiceChannelContextManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Override
    public VoiceChannelContext createVoiceChannelContext(VoiceChannel voiceChannel, Role role) {
        AChannel channel = channelManagementService.loadChannel(voiceChannel.getIdLong());
        if(voiceChannelContextManagementService.getVoiceChannelContextForChannel(channel).isPresent()) {
            throw new VoiceChannelContextAlreadyExistsException();
        }
        ARole arole = roleManagementService.findRole(role.getIdLong());
        return voiceChannelContextManagementService.createVoiceChannelContext(channel, arole);
    }

    @Override
    public void deleteVoiceChannelContext(AChannel channel) {
        Optional<VoiceChannelContext> ctxOptional = voiceChannelContextManagementService.getVoiceChannelContextForChannel(channel);
        if(!ctxOptional.isPresent()) {
            throw new VoiceChannelContextNotExistsException();
        }
        ctxOptional.ifPresent(context -> voiceChannelContextManagementService.deleteVoiceChannelContext(context));
    }
}
