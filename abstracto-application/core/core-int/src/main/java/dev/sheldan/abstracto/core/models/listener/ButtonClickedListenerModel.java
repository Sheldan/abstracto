package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.template.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

@Getter
@Setter
@Builder
public class ButtonClickedListenerModel implements FeatureAwareListenerModel {

    private ButtonClickEvent event;
    private String payload;
    private String origin;
    private ButtonPayload deserializedPayload;

    @Override
    public Long getServerId() {
        return event.getGuild().getIdLong();
    }
}
