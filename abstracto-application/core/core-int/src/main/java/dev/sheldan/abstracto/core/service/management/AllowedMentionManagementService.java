package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AllowedMention;

import java.util.Optional;

public interface AllowedMentionManagementService {
    Optional<AllowedMention> getCustomAllowedMentionFor(Long serverId);
    Optional<AllowedMention> getCustomAllowedMentionFor(AServer server);
    boolean hasCustomAllowedMention(Long serverId);
    AllowedMention createCustomAllowedMention(Long server, AllowedMention base);
    void deleteCustomAllowedMention(Long serverId);
}
