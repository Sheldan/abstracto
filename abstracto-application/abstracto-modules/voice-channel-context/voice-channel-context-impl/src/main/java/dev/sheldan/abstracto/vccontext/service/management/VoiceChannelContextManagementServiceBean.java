package dev.sheldan.abstracto.vccontext.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.vccontext.model.VoiceChannelContext;
import dev.sheldan.abstracto.vccontext.repository.VoiceChannelContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VoiceChannelContextManagementServiceBean implements VoiceChannelContextManagementService {

    @Autowired
    private VoiceChannelContextRepository repository;

    @Override
    public VoiceChannelContext createVoiceChannelContext(AChannel channel, ARole role) {
        VoiceChannelContext context = VoiceChannelContext
                .builder()
                .id(channel.getId())
                .role(role)
                .channel(channel)
                .build();
        return repository.save(context);
    }

    @Override
    public void deleteVoiceChannelContext(AChannel channel) {
        repository.deleteById(channel.getId());
    }

    @Override
    public void deleteVoiceChannelContext(VoiceChannelContext context) {
        repository.delete(context);
    }

    @Override
    public Optional<VoiceChannelContext> getVoiceChannelContextForChannel(AChannel channel) {
        return repository.findById(channel.getId());
    }
}
