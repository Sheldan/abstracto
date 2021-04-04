package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfanityGroupRepository extends JpaRepository<ProfanityGroup, Long> {
    Optional<ProfanityGroup> findByServerAndGroupNameIgnoreCase(AServer server, String name);
    void deleteByServerAndGroupNameIgnoreCase(AServer server, String name);
    List<ProfanityGroup> findByServer_Id(Long serverId);
}
