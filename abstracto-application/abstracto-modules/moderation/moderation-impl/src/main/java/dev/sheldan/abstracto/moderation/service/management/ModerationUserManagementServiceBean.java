package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.ModerationUser;
import dev.sheldan.abstracto.moderation.repository.ModerationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class ModerationUserManagementServiceBean implements ModerationUserManagementService {

    @Autowired
    private ModerationUserRepository repository;

    @Override
    public ModerationUser createModerationUser(AUserInAServer aUserInAServer) {
        ModerationUser moderationUser = ModerationUser
                .builder()
                .id(aUserInAServer.getUserInServerId())
                .user(aUserInAServer)
                .server(aUserInAServer.getServerReference())
                .build();
        return repository.save(moderationUser);
    }

    @Override
    public ModerationUser createModerationUserWithReportTimeStamp(AUserInAServer aUserInAServer, Instant reportTime) {
        ModerationUser moderationUser = createModerationUser(aUserInAServer);
        moderationUser.setLastReportTimeStamp(reportTime);
        return moderationUser;
    }

    @Override
    public Optional<ModerationUser> findModerationUser(AUserInAServer aUserInAServer) {
        return repository.findById(aUserInAServer.getUserInServerId());
    }
}
