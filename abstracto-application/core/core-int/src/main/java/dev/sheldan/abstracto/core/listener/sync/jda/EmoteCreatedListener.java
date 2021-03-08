package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import net.dv8tion.jda.api.entities.Emote;

public interface EmoteCreatedListener extends FeatureAware, Prioritized {
    void emoteCreated(Emote createdEmote);
}
