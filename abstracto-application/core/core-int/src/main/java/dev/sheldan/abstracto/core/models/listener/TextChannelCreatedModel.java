package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@Builder
public class TextChannelCreatedModel implements FeatureAwareListenerModel {
    private TextChannel channel;

    @Override
    public Long getServerId() {
        return channel.getGuild().getIdLong();
    }
}
