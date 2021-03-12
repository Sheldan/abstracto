package dev.sheldan.abstracto.repostdetection.service;

import dev.sheldan.abstracto.core.models.cache.CachedAttachment;
import dev.sheldan.abstracto.core.models.cache.CachedEmbed;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.model.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.repostdetection.model.database.PostedImage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RepostService {
    boolean isRepost(CachedMessage message, CachedEmbed messageEmbed, Integer embedIndex);
    Optional<PostedImage> getRepostFor(CachedMessage message, CachedEmbed messageEmbed, Integer embedIndex);
    Optional<PostedImage> getRepostFor(Message message, MessageEmbed messageEmbed, Integer embedIndex);
    boolean isRepost(CachedMessage message, CachedAttachment attachment, Integer index);
    Optional<PostedImage> getRepostFor(CachedMessage message, CachedAttachment attachment, Integer index);
    String calculateHashForPost(String url, Long serverId);
    void processMessageAttachmentRepostCheck(CachedMessage message);
    void processMessageEmbedsRepostCheck(List<CachedEmbed> embeds, CachedMessage message);
    void processMessageEmbedsRepostCheck(List<MessageEmbed> embeds, Message message);
    CompletableFuture<List<RepostLeaderboardEntryModel>> retrieveRepostLeaderboard(Guild guild, Integer page);
    void purgeReposts(AUserInAServer userInAServer);
    void purgeReposts(Guild guild);
}
