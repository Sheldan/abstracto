package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

public interface ReactedAddedListener extends FeatureAware {
    void executeReactionAdded(CachedMessage message, GuildMessageReactionAddEvent event, AUserInAServer userAdding);
}
