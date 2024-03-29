package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.repository.ExperienceRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExperienceRoleManagementServiceBean implements ExperienceRoleManagementService {

    @Autowired
    private ExperienceRoleRepository experienceRoleRepository;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    /**
     * Removes *all* assignments of roles for the given level
     * @param level The level to remove the roles for
     * @param server The server in which this should happen
     */
    @Override
    public void removeAllRoleAssignmentsForLevelInServerExceptRole(AExperienceLevel level, AServer server, ARole role) {
        List<AExperienceRole> existingExperienceRoles = experienceRoleRepository.findByLevelAndRoleServer(level, server);
        log.info("Removing all role assignments ({}) for level {} in server {}.", existingExperienceRoles.size(), level.getLevel(), server.getId());
        existingExperienceRoles.forEach(existingRole -> {
            if(!existingRole.getRole().getId().equals(role.getId())) {
                experienceRoleRepository.delete(existingRole);
            }
        });
    }

    @Override
    public void unsetRole(AExperienceRole role) {
        log.info("Deleting experience role {} in server {}.", role.getId(), role.getServer().getId());
        experienceRoleRepository.delete(role);
    }

    @Override
    public void unsetRoles(List<AExperienceRole> roles) {
        log.info("Deleting {} roles.", roles.size());
        roles.forEach(experienceRole -> {
            userExperienceManagementService.removeExperienceRoleFromUsers(experienceRole);
        });
        experienceRoleRepository.deleteAll(roles);
    }

    @Override
    public AExperienceRole getRoleInServer(ARole role) {
        // TODO throw different exception
        return this.getRoleInServerOptional(role).orElseThrow(AbstractoRunTimeException::new);
    }

    @Override
    public List<AExperienceRole> getRolesInServer(List<ARole> roles) {
        List<Long> roleIds = roles
                .stream()
                .map(ARole::getId)
                .collect(Collectors.toList());
        return experienceRoleRepository.findByRole_IdIn(roleIds);
    }

    @Override
    public Optional<AExperienceRole> getRoleInServerOptional(ARole role) {
        return experienceRoleRepository.findByRole(role);
    }

    @Override
    public List<AExperienceRole> getExperienceRolesForServer(AServer server) {
        return experienceRoleRepository.findByRoleServer(server);
    }

    @Override
    public AExperienceRole getExperienceRoleById(Long experienceRoleId) {
        // TODO throw different exception
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
        log.info("Setting role {} in server {} to level {}.", role.getId(), role.getServer().getId(), level.getLevel());
        if(byRoleServerAndRoleOptional.isPresent()) {
            log.debug("Role already existed. Updating.");
            experienceRole = byRoleServerAndRoleOptional.get();
            experienceRole.setLevel(level);
        } else {
            experienceRole = AExperienceRole
                    .builder()
                    .id(role.getId())
                    .level(level)
                    .server(role.getServer())
                    .role(role)
                    .build();
            log.debug("Role did not exist. Creating new.");
            return experienceRoleRepository.save(experienceRole);
        }
        return experienceRole;
    }

    @Override
    public List<AExperienceRole> getExperienceRolesAtLevelInServer(AExperienceLevel level, AServer server) {
        return experienceRoleRepository.findByLevelAndRoleServer(level, server);
    }
}
