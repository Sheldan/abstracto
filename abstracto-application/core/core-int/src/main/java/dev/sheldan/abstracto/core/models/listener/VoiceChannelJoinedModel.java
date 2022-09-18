package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

@Getter
@Setter
@Builder
public class VoiceChannelJoinedModel implements FeatureAwareListenerModel {

    private Member member;
    private AudioChannel channel;

    @Override
    public Long getServerId() {
        return channel.getGuild().getIdLong();
    }
}
