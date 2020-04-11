package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.repository.ExperienceRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExperienceRoleManagementServiceBean implements ExperienceRoleManagementService {

    @Autowired
    private ExperienceRoleRepository experienceRoleRepository;

    @Override
    public void unSetLevelInServer(AExperienceLevel level, AServer server) {
        List<AExperienceRole> existingExperienceRoles = experienceRoleRepository.findByLevelAndRoleServer(level, server);
        existingExperienceRoles.forEach(existingRole -> {
            experienceRoleRepository.delete(existingRole);
        });
    }

    @Override
    public List<AExperienceRole> getExperienceRoleForServer(AServer server) {
        return experienceRoleRepository.findByRoleServer(server);
    }

    @Override
    public void setLevelToRole(AExperienceLevel level, ARole role, AServer server) {
        AExperienceRole byRoleServerAndRole = experienceRoleRepository.findByRoleServerAndRole(server, role);
        if(byRoleServerAndRole != null) {
            byRoleServerAndRole.setLevel(level);
        } else {
            byRoleServerAndRole = AExperienceRole
                    .builder()
                    .level(level)
                    .roleServer(server)
                    .role(role)
                    .build();
        }
        experienceRoleRepository.save(byRoleServerAndRole);
    }
}
