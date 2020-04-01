package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.utility.models.StarboardPost;
import dev.sheldan.abstracto.utility.models.StarboardPostReaction;
import dev.sheldan.abstracto.utility.models.template.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.repository.StarStatsUserResult;
import dev.sheldan.abstracto.utility.repository.StarboardPostReactionRepository;
import dev.sheldan.abstracto.utility.repository.converter.StarStatsUserConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class StarboardPostReactorManagementServiceBean implements StarboardPostReactorManagementService {

    @Autowired
    private StarboardPostReactionRepository repository;

    @Autowired
    private StarStatsUserConverter converter;

    @Override
    public void addReactor(StarboardPost post, AUser user) {
        StarboardPostReaction reactor = StarboardPostReaction
                .builder()
                .starboardPost(post)
                .reactor(user)
                .build();
        repository.save(reactor);
    }

    @Override
    public void removeReactor(StarboardPost post, AUser user) {
        repository.deleteByReactorAndStarboardPost(user, post);
    }

    @Override
    public void removeReactors(StarboardPost post) {
        repository.deleteByStarboardPost(post);
    }

    @Override
    public Integer getStarCount(Long serverId) {
        return repository.getReactionCountByServer(serverId);
    }

    @Override
    public List<StarStatsUser> retrieveTopStarGiver(Long serverId, Integer count) {
        List<StarStatsUserResult> starGivers = repository.findTopStarGiverInServer(serverId, count);
        return converter.convertToStarStatsUser(starGivers, serverId);
    }

    @Override
    public List<StarStatsUser> retrieveTopStarReceiver(Long serverId, Integer count) {
        List<StarStatsUserResult> starReceivers = repository.retrieveTopStarReceiverInServer(serverId, count);
        return converter.convertToStarStatsUser(starReceivers, serverId);
    }

}
