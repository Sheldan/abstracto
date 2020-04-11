package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRoleRepository extends JpaRepository<AExperienceRole, Long> {
    AExperienceRole findByRoleServerAndRole(AServer server, ARole role);
    List<AExperienceRole> findByLevelAndRoleServer(AExperienceLevel level, AServer server);
    List<AExperienceRole> findByRoleServer(AServer server);
}
