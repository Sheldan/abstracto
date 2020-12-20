package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;

public interface ReactionRemovedListener extends FeatureAware, Prioritized {
    void executeReactionRemoved(CachedMessage message, GuildMessageReactionRemoveEvent reaction, ServerUser serverUser);
}
