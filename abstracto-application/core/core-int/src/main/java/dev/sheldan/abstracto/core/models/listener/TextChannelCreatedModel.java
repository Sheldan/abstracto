package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@Getter
@Setter
@Builder
public class TextChannelCreatedModel implements FeatureAwareListenerModel {
    private Channel channel;

    @Override
    public Long getServerId() {
        return channel instanceof GuildChannel ? ((GuildChannel)channel).getGuild().getIdLong() : null;
    }
}
