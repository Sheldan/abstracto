package dev.sheldan.abstracto.core.listener;

import net.dv8tion.jda.api.entities.Emote;

public interface EmoteUpdatedListener extends FeatureAware, Prioritized {
    void emoteUpdated(Emote updatedEmote, String oldValue, String newValue);
}
