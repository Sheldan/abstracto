package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import java.util.List;
import java.util.Optional;

public interface QuickReplyManagementService {
    Optional<QuickReply> getQuickReplyByName(String name, Long serverId);
    QuickReply createQuickReply(String name, String content, AUserInAServer creator, boolean anonymous);
    void deleteQuickReply(String name, AServer server);
    List<QuickReply> getQuickReplies(AServer server);
    List<QuickReply> getQuickRepliesContaining(String prefix, AServer server);
}
