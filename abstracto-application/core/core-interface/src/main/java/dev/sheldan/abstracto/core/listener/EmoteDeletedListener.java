package dev.sheldan.abstracto.core.listener;

import net.dv8tion.jda.api.entities.Emote;

public interface EmoteDeletedListener extends FeatureAware, Prioritized {
    void emoteDeleted(Emote deletedEmote);
}
