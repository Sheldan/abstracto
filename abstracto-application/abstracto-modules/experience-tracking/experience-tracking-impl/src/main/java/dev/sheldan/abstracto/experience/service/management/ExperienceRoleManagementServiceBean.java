package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.repository.ExperienceRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ExperienceRoleManagementServiceBean implements ExperienceRoleManagementService {

    @Autowired
    private ExperienceRoleRepository experienceRoleRepository;

    /**
     * Removes *all* assignments of roles for the given level
     * @param level The level to remove the roles for
     * @param server The server in which this should happen
     */
    @Override
    public void removeAllRoleAssignmentsForLevelInServer(AExperienceLevel level, AServer server) {
        log.trace("Removing all role assignments for level {}.", level.getLevel());
        List<AExperienceRole> existingExperienceRoles = experienceRoleRepository.findByLevelAndRoleServer(level, server);
        existingExperienceRoles.forEach(existingRole -> experienceRoleRepository.delete(existingRole));
    }

    @Override
    public void unsetRole(AExperienceRole role) {
        experienceRoleRepository.delete(role);
    }

    @Override
    public AExperienceRole getRoleInServer(ARole role) {
        return experienceRoleRepository.findByRole(role);
    }

    @Override
    public List<AExperienceRole> getExperienceRolesForServer(AServer server) {
        return experienceRoleRepository.findByRoleServer(server);
    }

    /**
     * Creates a new role if nothing is found, and if its found the experience role will be set to the given level.
     * @param level The {@link AExperienceLevel} to set the role for
     * @param role The {@link ARole} to set to
     * @return The created/updated {@link AExperienceRole}
     */
    @Override
    public AExperienceRole setLevelToRole(AExperienceLevel level, ARole role) {
        AExperienceRole byRoleServerAndRole = experienceRoleRepository.findByRole(role);
        if(byRoleServerAndRole != null) {
            byRoleServerAndRole.setLevel(level);
        } else {
            byRoleServerAndRole = AExperienceRole
                    .builder()
                    .level(level)
                    .roleServer(role.getServer())
                    .role(role)
                    .build();
            byRoleServerAndRole = experienceRoleRepository.save(byRoleServerAndRole);
        }
        return byRoleServerAndRole;
    }
}
