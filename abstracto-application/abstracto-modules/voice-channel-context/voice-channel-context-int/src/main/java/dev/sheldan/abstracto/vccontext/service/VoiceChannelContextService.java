package dev.sheldan.abstracto.vccontext.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.vccontext.model.VoiceChannelContext;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public interface VoiceChannelContextService {
    VoiceChannelContext createVoiceChannelContext(VoiceChannel voiceChannel, Role role);
    void deleteVoiceChannelContext(AChannel channel);
}
