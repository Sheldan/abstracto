package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleManagementServiceBean implements RoleManagementService {

    @Autowired
    private RoleRepository repository;

    @Override
    public ARole createRole(Long id, AServer server) {
        return repository.save(ARole.builder().id(id).server(server).deleted(false).build());
    }

    @Override
    public ARole findRole(Long id) {
        return repository.getOne(id);
    }

    @Override
    public void markDeleted(ARole role) {
        role.setDeleted(true);
        repository.save(role);
    }
}
