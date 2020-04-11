package dev.sheldan.abstracto.experience.service.management;


import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;

public interface UserExperienceManagementService {
    void setExperienceTo(AUserExperience aUserInAServer, Long experience);
    AUserExperience findUserInServer(AUserInAServer aUserInAServer);
    AUserExperience createUserInServer(AUserInAServer aUserInAServer);
    void saveUser(AUserExperience userExperience);
}
