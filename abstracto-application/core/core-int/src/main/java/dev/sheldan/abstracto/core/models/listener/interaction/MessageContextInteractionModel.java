package dev.sheldan.abstracto.core.models.listener.interaction;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

@Getter
@Setter
@Builder
public class MessageContextInteractionModel implements FeatureAwareListenerModel {
    private MessageContextInteractionEvent event;

    @Override
    public Long getServerId() {
        return ContextUtils.hasGuild(event) ? event.getGuild().getIdLong() : null;
    }
}
