package dev.sheldan.abstracto.repository;

import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUser;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInServerRepository extends JpaRepository<AUserInAServer, Long> {
    AUserInAServer findByServerReferenceAndUserReference(AServer serverId, AUser userId);
}