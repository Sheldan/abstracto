package dev.sheldan.abstracto.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInServerRepository extends JpaRepository<AUserInAServer, Long> {
    AUserInAServer findByServerReferenceAndUserReference(AServer serverId, AUser userId);
}