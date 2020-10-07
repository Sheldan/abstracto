package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.RoleNotFoundInDBException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class RoleManagementServiceBean implements RoleManagementService {

    @Autowired
    private RoleRepository repository;

    @Override
    public ARole createRole(Long id, AServer server) {
        ARole build = ARole
                .builder()
                .id(id)
                .server(server)
                .deleted(false)
                .build();
        server.getRoles().add(build);
        log.info("Creating role {} in server {}.", id, server.getId());
        return repository.save(build);
    }

    @Override
    public Optional<ARole> findRoleOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public ARole findRole(Long id) {
        return findRoleOptional(id).orElseThrow(() -> new RoleNotFoundInDBException(id));
    }

    @Override
    public void markDeleted(ARole role) {
        log.info("Marking role {} in server {} as deleted.", role.getId(), role.getServer().getId());
        role.setDeleted(true);
    }
}
