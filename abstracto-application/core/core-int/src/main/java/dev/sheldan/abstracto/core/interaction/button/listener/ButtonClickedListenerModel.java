package dev.sheldan.abstracto.core.interaction.button.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Getter
@Setter
@Builder
public class ButtonClickedListenerModel implements FeatureAwareListenerModel {

    private ButtonInteractionEvent event;
    private String payload;
    private String origin;
    private ButtonPayload deserializedPayload;

    @Override
    public Long getServerId() {
        return event.isFromGuild() ? event.getGuild().getIdLong() : null;
    }
}
