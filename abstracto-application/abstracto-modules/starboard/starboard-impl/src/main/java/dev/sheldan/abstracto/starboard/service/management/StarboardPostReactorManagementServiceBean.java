package dev.sheldan.abstracto.starboard.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.starboard.converter.StarStatsUserConverter;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.model.database.StarboardPostReaction;
import dev.sheldan.abstracto.starboard.model.template.StarStatsUser;
import dev.sheldan.abstracto.starboard.repository.result.StarStatsGuildUserResult;
import dev.sheldan.abstracto.starboard.repository.StarboardPostReactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class StarboardPostReactorManagementServiceBean implements StarboardPostReactorManagementService {

    @Autowired
    private StarboardPostReactionRepository repository;

    @Autowired
    private StarStatsUserConverter converter;

    @Override
    public StarboardPostReaction addReactor(StarboardPost post, AUserInAServer user) {
        StarboardPostReaction reactor = StarboardPostReaction
                .builder()
                .starboardPost(post)
                .reactor(user)
                .server(user.getServerReference())
                .build();
        log.info("Persisting the reactor {} for starboard post {} in server {}.", user.getUserReference().getId(), post.getId(), user.getServerReference().getId());
        repository.save(reactor);
        return reactor;
    }

    @Override
    public void removeReactor(StarboardPost post, AUserInAServer user) {
        log.info("Removing reactor {} from post {} in server {}.", user.getUserReference().getId(),  post.getId(), user.getServerReference().getId());
        repository.deleteByReactorAndStarboardPost(user, post);
    }

    @Override
    public void removeReactors(StarboardPost post) {
        log.info("Removing all {} reactors from starboard post {}", post.getReactions().size(), post.getId());
        repository.deleteByStarboardPost(post);
    }

    @Override
    public Integer getStarCount(Long serverId) {
        return repository.getReactionCountByServer(serverId);
    }

    @Override
    public List<CompletableFuture<StarStatsUser>> retrieveTopStarGiver(Long serverId, Integer count) {
        List<StarStatsGuildUserResult> starGivers = repository.findTopStarGiverInServer(serverId, count);
        return converter.convertToStarStatsUser(starGivers, serverId);
    }

    @Override
    public List<CompletableFuture<StarStatsUser>> retrieveTopStarReceiver(Long serverId, Integer count) {
        List<StarStatsGuildUserResult> starReceivers = repository.retrieveTopStarReceiverInServer(serverId, count);
        return converter.convertToStarStatsUser(starReceivers, serverId);
    }

}
