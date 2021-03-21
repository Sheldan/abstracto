package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;

@Getter
@Setter
@Builder
public class ReactionRemovedModel implements FeatureAwareListenerModel {
    private CachedMessage message;
    private MessageReaction reaction;
    private ServerUser userRemoving;
    private Member memberRemoving;

    @Override
    public Long getServerId() {
        return userRemoving.getServerId();
    }
}
