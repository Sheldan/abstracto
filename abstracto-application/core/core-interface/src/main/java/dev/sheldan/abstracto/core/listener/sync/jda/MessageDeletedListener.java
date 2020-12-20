package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;

public interface MessageDeletedListener extends FeatureAware, Prioritized {
    void execute(CachedMessage messageBefore, AServerAChannelAUser authorUser, GuildChannelMember authorMember);
}
