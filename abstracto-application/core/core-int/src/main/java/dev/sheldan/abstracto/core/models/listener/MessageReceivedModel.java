package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
@Builder
public class MessageReceivedModel implements FeatureAwareListenerModel {
    private Message message;
    @Override
    public Long getServerId() {
        return message.getGuild().getIdLong();
    }
}
