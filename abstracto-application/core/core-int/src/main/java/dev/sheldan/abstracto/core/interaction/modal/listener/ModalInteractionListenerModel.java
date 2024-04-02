package dev.sheldan.abstracto.core.interaction.modal.listener;

import dev.sheldan.abstracto.core.interaction.modal.ModalPayload;
import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

@Getter
@Setter
@Builder
public class ModalInteractionListenerModel implements FeatureAwareListenerModel {
    private ModalInteractionEvent event;
    private String payload;
    private String origin;
    private ModalPayload deserializedPayload;

    @Override
    public Long getServerId() {
        return ContextUtils.hasGuild(event) ? event.getGuild().getIdLong() : null;
    }
}
