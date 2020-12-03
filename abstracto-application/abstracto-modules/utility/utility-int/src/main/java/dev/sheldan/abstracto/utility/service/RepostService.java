package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.utility.models.database.PostedImage;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RepostService {
    boolean isRepost(Message message, MessageEmbed messageEmbed, Integer embedIndex);
    Optional<PostedImage> getRepostFor(Message message, MessageEmbed messageEmbed, Integer embedIndex);
    boolean isRepost(Message message, Message.Attachment attachment, Integer index);
    Optional<PostedImage> getRepostFor(Message message, Message.Attachment attachment, Integer index);
    String calculateHashForPost(String url, Long serverId);
    void processMessageAttachmentRepostCheck(Message message);
    void processMessageEmbedsRepostCheck(List<MessageEmbed> embeds, Message message);
    CompletableFuture<List<RepostLeaderboardEntryModel>> retrieveRepostLeaderboard(Guild guild, Integer page);
    void purgeReposts(AUserInAServer userInAServer);
    void purgeReposts(Guild guild);
}
