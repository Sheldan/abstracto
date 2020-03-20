package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.PostTarget;
import net.dv8tion.jda.api.entities.MessageEmbed;

public interface PostTargetService {
    void sendTextInPostTarget(String text, PostTarget target);
    void sendEmbedInPostTarget(MessageEmbed embed, PostTarget target);
    void sendTextInPostTarget(String text, String postTargetName, Long serverId);
    void sendEmbedInPostTarget(MessageEmbed embed, String postTargetName, Long serverId);
}
