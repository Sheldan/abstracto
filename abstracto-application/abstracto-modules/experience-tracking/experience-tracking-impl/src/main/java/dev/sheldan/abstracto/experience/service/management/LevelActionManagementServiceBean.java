package dev.sheldan.abstracto.experience.service.management;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.LevelActionPayload;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import dev.sheldan.abstracto.experience.repository.LevelActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LevelActionManagementServiceBean implements LevelActionManagementService {

    @Autowired
    private LevelActionRepository levelActionRepository;

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Autowired
    private Gson gson;

    @Override
    public LevelAction createLevelAction(Integer level, AServer server, String action, AUserExperience user, String payload) {
        AExperienceLevel experienceLevel = experienceLevelManagementService.getLevel(level);
        LevelAction levelAction = LevelAction
                .builder()
                .action(action)
                .affectedUser(user)
                .payload(payload)
                .server(server)
                .level(experienceLevel)
                .build();
        return levelActionRepository.save(levelAction);
    }

    @Override
    public void deleteLevelAction(Integer level, AServer server, String action, AUserExperience user) {
        AExperienceLevel experienceLevel = experienceLevelManagementService.getLevel(level);
        if(user == null) {
            levelActionRepository.deleteByLevelAndActionAndServer(experienceLevel, action, server);
        } else {
            levelActionRepository.deleteByLevelAndActionAndAffectedUser(experienceLevel, action, user);
        }
    }

    @Override
    public LevelAction createLevelAction(Integer level, AServer server, String action, AUserExperience user, LevelActionPayload actionPayload) {
        String payload = gson.toJson(actionPayload);
        return createLevelAction(level, server, action, user, payload);
    }

    @Override
    public List<LevelAction> getLevelActionsOfUserInServer(AUserExperience aUserInAServer) {
        return levelActionRepository.findByServerAndAffectedUserIsNullOrServerAndAffectedUser(aUserInAServer.getServer(),
                aUserInAServer.getServer(), aUserInAServer);
    }

    @Override
    public List<LevelAction> getLevelActionsOfServer(AServer server) {
        return levelActionRepository.findByServer(server);
    }

    @Override
    public Optional<LevelAction> getLevelAction(String action, Integer level, AServer server, AUserExperience aUserExperience) {
        AExperienceLevel experienceLevel = experienceLevelManagementService.getLevel(level);
        return levelActionRepository.findByServerAndActionAndLevelOrAffectedUserAndLevelAndAction(server, action.toLowerCase(),
                experienceLevel, aUserExperience, experienceLevel, action.toLowerCase());
    }
}
