package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

public interface ReactionAddedListener extends FeatureAware, Prioritized {
    void executeReactionAdded(CachedMessage message, GuildMessageReactionAddEvent event, ServerUser serverUser);
}
