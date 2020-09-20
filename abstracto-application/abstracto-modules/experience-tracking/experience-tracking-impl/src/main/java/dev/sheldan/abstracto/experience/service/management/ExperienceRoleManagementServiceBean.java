package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.repository.ExperienceRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ExperienceRoleManagementServiceBean implements ExperienceRoleManagementService {

    @Autowired
    private ExperienceRoleRepository experienceRoleRepository;

    @Autowired
    private RoleManagementService roleManagementService;

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
        return this.getRoleInServerOptional(role).orElseThrow(AbstractoRunTimeException::new);
    }

    @Override
    public Optional<AExperienceRole> getRoleInServerOptional(ARole role) {
        return experienceRoleRepository.findByRole(role);
    }

    @Override
    public AExperienceRole getRoleInServer(Long roleId) {
        ARole role = roleManagementService.findRole(roleId);
        return getRoleInServer(role);
    }

    @Override
    public List<AExperienceRole> getExperienceRolesForServer(AServer server) {
        return experienceRoleRepository.findByRoleServer(server);
    }

    @Override
    public AExperienceRole getExperienceRoleById(Long experienceRoleId) {
        return getExperienceRoleByIdOptional(experienceRoleId).orElseThrow(() -> new AbstractoRunTimeException("Experience role not found"));
    }

    @Override
    public Optional<AExperienceRole> getExperienceRoleByIdOptional(Long experienceRoleId) {
        return experienceRoleRepository.findById(experienceRoleId);
    }

    /**
     * Creates a new role if nothing is found, and if its found the experience role will be set to the given level.
     * @param level The {@link AExperienceLevel} to set the role for
     * @param role The {@link ARole} to set to
     * @return The created/updated {@link AExperienceRole}
     */
    @Override
    public AExperienceRole setLevelToRole(AExperienceLevel level, ARole role) {
        Optional<AExperienceRole> byRoleServerAndRoleOptional = getRoleInServerOptional(role);
        AExperienceRole experienceRole;
        if(byRoleServerAndRoleOptional.isPresent()) {
            experienceRole = byRoleServerAndRoleOptional.get();
            experienceRole.setLevel(level);
        } else {
            experienceRole = AExperienceRole
                    .builder()
                    .level(level)
                    .roleServer(role.getServer())
                    .role(role)
                    .build();
            experienceRole = experienceRoleRepository.save(experienceRole);
        }
        return experienceRole;
    }
}
