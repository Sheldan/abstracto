package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInServerRepository extends JpaRepository<AUserInAServer, Long> {

    Optional<AUserInAServer> findByServerReferenceAndUserReference(AServer serverId, AUser userId);

    Optional<AUserInAServer> findByServerReference_IdAndUserReference_Id(Long serverId, Long userId);

    boolean existsByServerReferenceAndUserReference(AServer server, AUser user);

    boolean existsByServerReference_IdAndUserReference_Id(Long serverId, Long userId);

    List<AUserInAServer> findByUserReference(AUser user);
}