package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.RoleNotFoundInDBException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.ARoleCreatedListenerModel;
import dev.sheldan.abstracto.core.models.listener.ARoleDeletedListenerModel;
import dev.sheldan.abstracto.core.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class RoleManagementServiceBean implements RoleManagementService {

    @Autowired
    private RoleRepository repository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public ARole createRole(Long id, AServer server) {
        ARole build = ARole
                .builder()
                .id(id)
                .server(server)
                .deleted(false)
                .build();
        log.info("Creating role {} in server {}.", id, server.getId());
        ARole createdRole = repository.save(build);
        ARoleCreatedListenerModel model = getCreationModel(createdRole);
        eventPublisher.publishEvent(model);
        return createdRole;
    }

    @Override
    public Optional<ARole> findRoleOptional(Long id) {
        return repository.findById(id);
    }

    private ARoleCreatedListenerModel getCreationModel(ARole createdRole) {
        return ARoleCreatedListenerModel
                .builder()
                .roleId(createdRole.getId())
                .build();
    }

    private ARoleDeletedListenerModel getDeletionModel(ARole deletedRole) {
        return ARoleDeletedListenerModel
                .builder()
                .roleId(deletedRole.getId())
                .build();
    }

    @Override
    public ARole findRole(Long id) {
        return findRoleOptional(id).orElseThrow(() -> new RoleNotFoundInDBException(id));
    }

    @Override
    public void markDeleted(ARole role) {
        log.info("Marking role {} in server {} as deleted.", role.getId(), role.getServer().getId());
        ARoleDeletedListenerModel model = getDeletionModel(role);
        eventPublisher.publishEvent(model);
        role.setDeleted(true);
    }

    @Override
    public void markDeleted(Long roleId) {
        markDeleted(findRole(roleId));
    }
}
