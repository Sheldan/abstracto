package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class VoiceChannelLeftModel implements FeatureAwareListenerModel {

    private Member member;
    private AudioChannel channel;

    @Override
    public Long getServerId() {
        return channel.getGuild().getIdLong();
    }
}
