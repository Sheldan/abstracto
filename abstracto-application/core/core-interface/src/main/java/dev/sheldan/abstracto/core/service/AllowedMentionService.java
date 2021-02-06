package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AllowedMention;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface AllowedMentionService {
    boolean allMentionsAllowed(Long serverId);
    List<Message.MentionType> getAllowedMentionTypesForServer(Long serverId);
    void allowMentionForServer(Message.MentionType mentionType, Long serverId);
    void disAllowMentionForServer(Message.MentionType mentionType, Long serverId);
    AllowedMention getDefaultAllowedMention();
    AllowedMention getEffectiveAllowedMention(Long serverId);
    Message.MentionType getMentionTypeFromString(String input);
}
