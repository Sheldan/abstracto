package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AUser;
import dev.sheldan.abstracto.core.models.converter.UserConverter;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.database.StarboardPostReaction;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.repository.StarStatsUserResult;
import dev.sheldan.abstracto.utility.repository.StarboardPostReactionRepository;
import dev.sheldan.abstracto.utility.repository.converter.StarStatsUserConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StarboardPostReactorManagementServiceBean {

    @Autowired
    private StarboardPostReactionRepository repository;

    @Autowired
    private StarStatsUserConverter converter;

    @Autowired
    private UserConverter userConverter;

    public void addReactor(StarboardPost post, UserDto user) {
        AUser aUser = userConverter.toUser(user);
        StarboardPostReaction reactor = StarboardPostReaction
                .builder()
                .starboardPost(post)
                .reactor(aUser)
                .build();
        repository.save(reactor);
    }

    public void removeReactor(StarboardPost post, UserDto user) {
        AUser aUser = userConverter.toUser(user);
        repository.deleteByReactorAndStarboardPost(aUser, post);
    }

    public void removeReactors(StarboardPost post) {
        repository.deleteByStarboardPost(post);
    }

    public Integer getStarCount(Long serverId) {
        return repository.getReactionCountByServer(serverId);
    }

    public List<StarStatsUser> retrieveTopStarGiver(Long serverId, Integer count) {
        List<StarStatsUserResult> starGivers = repository.findTopStarGiverInServer(serverId, count);
        return converter.convertToStarStatsUser(starGivers, serverId);
    }

    public List<StarStatsUser> retrieveTopStarReceiver(Long serverId, Integer count) {
        List<StarStatsUserResult> starReceivers = repository.retrieveTopStarReceiverInServer(serverId, count);
        return converter.convertToStarStatsUser(starReceivers, serverId);
    }

}
