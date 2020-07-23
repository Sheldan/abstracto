package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;

public interface ReactedRemovedListener extends FeatureAware {
    void executeReactionRemoved(CachedMessage message, GuildMessageReactionRemoveEvent reaction, AUserInAServer userRemoving);
}
