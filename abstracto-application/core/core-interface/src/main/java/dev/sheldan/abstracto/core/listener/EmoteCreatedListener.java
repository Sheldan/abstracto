package dev.sheldan.abstracto.core.listener;

import net.dv8tion.jda.api.entities.Emote;

public interface EmoteCreatedListener extends FeatureAware, Prioritized {
    void emoteCreated(Emote createdEmote);
}
