package dev.sheldan.abstracto.vccontext.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.vccontext.model.VoiceChannelContext;

import java.util.Optional;

public interface VoiceChannelContextManagementService {
    VoiceChannelContext createVoiceChannelContext(AChannel channel, ARole role);
    void deleteVoiceChannelContext(AChannel channel);
    void deleteVoiceChannelContext(VoiceChannelContext context);
    Optional<VoiceChannelContext> getVoiceChannelContextForChannel(AChannel channel);
}
