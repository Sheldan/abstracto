package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Emote;

@Getter
@Setter
@Builder
public class EmoteNameUpdatedModel implements FeatureAwareListenerModel {

    private Emote emote;
    private String newValue;
    private String oldValue;

    @Override
    public Long getServerId() {
        return emote.getGuild().getIdLong();
    }
}
