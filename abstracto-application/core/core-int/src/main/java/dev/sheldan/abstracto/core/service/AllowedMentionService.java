package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AllowedMention;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.Set;

public interface AllowedMentionService {
    boolean allMentionsAllowed(Long serverId);
    Set<Message.MentionType> getAllowedMentionTypesForServer(Long serverId);
    void allowMentionForServer(Message.MentionType mentionType, Long serverId);
    void disAllowMentionForServer(Message.MentionType mentionType, Long serverId);
    AllowedMention getDefaultAllowedMention();
    AllowedMention getEffectiveAllowedMention(Long serverId);
    Message.MentionType getMentionTypeFromString(String input);
    Set<Message.MentionType> getAllowedMentionsFor(MessageChannel channel, MessageToSend messageToSend);
}
