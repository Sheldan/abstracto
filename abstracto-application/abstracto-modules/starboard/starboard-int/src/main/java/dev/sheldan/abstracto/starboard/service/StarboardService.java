package dev.sheldan.abstracto.starboard.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.model.template.GuildStarStatsModel;
import dev.sheldan.abstracto.starboard.model.template.MemberStarStatsModel;
import dev.sheldan.abstracto.starboard.model.template.StarStatsPost;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StarboardService {
    CompletableFuture<Void> createStarboardPost(CachedMessage message, List<AUserInAServer> userExceptAuthor, AUserInAServer userReacting, AUserInAServer starredUser);
    CompletableFuture<Void> updateStarboardPost(StarboardPost post, CachedMessage message, List<AUserInAServer> userExceptAuthor);
    void deleteStarboardMessagePost(StarboardPost message);
    CompletableFuture<GuildStarStatsModel> retrieveStarStats(Long serverId);
    MemberStarStatsModel retrieveStarStatsForMember(Member member);
    StarStatsPost fromStarboardPost(StarboardPost starboardPost);
}
