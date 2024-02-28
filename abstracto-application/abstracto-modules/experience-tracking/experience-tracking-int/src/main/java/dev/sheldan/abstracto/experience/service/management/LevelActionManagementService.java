package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.LevelActionPayload;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;

import java.util.List;
import java.util.Optional;

public interface LevelActionManagementService {
    LevelAction createLevelAction(Integer level, AServer server, String action, AUserExperience user, String parameters);
    void deleteLevelAction(Integer level, AServer server, String action, AUserExperience user);
    LevelAction createLevelAction(Integer level, AServer server, String action, AUserExperience user, LevelActionPayload actionPayload);

    List<LevelAction> getLevelActionsOfUserInServer(AUserExperience aUserInAServer);
    List<LevelAction> getLevelActionsOfServer(AServer server);

    Optional<LevelAction> getLevelAction(String action, Integer level, AServer server, AUserExperience aUserExperience);
}
