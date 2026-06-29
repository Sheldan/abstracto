package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public interface QuickReplyService {
    QuickReply createQuickReply(String name, String content, Member creator, boolean anonymous);
    void deleteQuickReply(String name, Guild guild);
    List<QuickReply> getQuickReplies(Guild guild);
    Optional<QuickReply> getQuickReply(String name, Guild guild);
    List<QuickReply> getQuickRepliesContaining(String name, Guild guild);
}
