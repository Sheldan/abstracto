package dev.sheldan.abstracto.entertainment.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.model.database.EconomyLeaderboardResult;
import dev.sheldan.abstracto.entertainment.model.database.EconomyUser;
import dev.sheldan.abstracto.entertainment.repository.EconomyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
public class EconomyUserManagementServiceBean implements EconomyUserManagementService {

    @Autowired
    private EconomyUserRepository repository;

    @Override
    public EconomyUser createUser(AUserInAServer aUserInAServer) {
        EconomyUser user = EconomyUser
                .builder()
                .id(aUserInAServer.getUserInServerId())
                .server(aUserInAServer.getServerReference())
                .credits(0L)
                .user(aUserInAServer)
                .build();
        return repository.save(user);
    }

    @Override
    public Optional<EconomyUser> getUser(AUserInAServer aUserInAServer) {
        return repository.findByUser(aUserInAServer);
    }

    @Override
    public Optional<EconomyUser> getUser(ServerUser serverUser) {
        return repository.findByServer_IdAndUser_UserReference_Id(serverUser.getServerId(), serverUser.getUserId());
    }

    @Override
    public EconomyLeaderboardResult getRankOfUserInServer(AUserInAServer aUserInAServer) {
        return repository.getRankOfUserInServer(aUserInAServer.getUserInServerId(), aUserInAServer.getServerReference().getId());
    }

    @Override
    public List<EconomyUser> getRanksInServer(AServer server, Integer page, Integer pagesize) {
        return repository.findTop10ByServerOrderByCreditsDesc(server, PageRequest.of(page, pagesize));
    }
}
