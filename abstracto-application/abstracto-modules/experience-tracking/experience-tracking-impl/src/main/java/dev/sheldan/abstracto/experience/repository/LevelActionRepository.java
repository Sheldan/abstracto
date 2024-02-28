package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelActionRepository extends JpaRepository<LevelAction, Long> {
    List<LevelAction> findByServerAndAffectedUserIsNullOrServerAndAffectedUser(AServer server, AServer server2, AUserExperience user);
    Optional<LevelAction> findByServerAndActionAndLevelOrAffectedUserAndLevelAndAction(AServer server, String action, AExperienceLevel level, AUserExperience user, AExperienceLevel level2, String action2);
    List<LevelAction> findByServer(AServer server);

    void deleteByLevelAndActionAndServer(AExperienceLevel level, String action, AServer server);
    void deleteByLevelAndActionAndAffectedUser(AExperienceLevel level, String action, AUserExperience affectedUser);

}
