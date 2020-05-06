package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModMailThreadRepository extends JpaRepository<ModMailThread, Long> {
    ModMailThread findByChannel(AChannel channel);
    List<ModMailThread> findByUser(AUserInAServer aUserInAServer);
    ModMailThread findTopByUserOrderByClosedDesc(AUserInAServer aUserInAServer);
    ModMailThread findByUser_UserReferenceAndStateNot(AUser user, ModMailThreadState state);
    List<ModMailThread> findByServerAndState(AServer server, ModMailThreadState state);
    ModMailThread findByUserAndStateNot(AUserInAServer userInAServer, ModMailThreadState state);
    List<ModMailThread> findByUserAndState(AUserInAServer userInAServer, ModMailThreadState state);
}
