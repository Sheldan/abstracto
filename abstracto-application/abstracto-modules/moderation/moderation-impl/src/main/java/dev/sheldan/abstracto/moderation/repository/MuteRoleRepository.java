package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.MuteRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MuteRoleRepository extends JpaRepository<MuteRole, Long> {

    MuteRole findByRoleServer(AServer server);

    List<MuteRole> findAllByRoleServer(AServer server);

    boolean existsByRoleServer(AServer server);
}
