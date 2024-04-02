package dev.sheldan.abstracto.core.interaction.menu.listener;

import dev.sheldan.abstracto.core.interaction.menu.SelectMenuPayload;
import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

@Getter
@Setter
@Builder
public class StringSelectMenuListenerModel implements FeatureAwareListenerModel {

    private StringSelectInteractionEvent event;
    private String payload;
    private String origin;
    private SelectMenuPayload deserializedPayload;

    @Override
    public Long getServerId() {
        return ContextUtils.hasGuild(event) ? event.getGuild().getIdLong() : null;
    }
}
