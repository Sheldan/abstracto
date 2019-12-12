package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.ARole;
import dev.sheldan.abstracto.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceBean implements RoleService {

    @Autowired
    private RoleRepository repository;

    @Override
    public ARole createRole(Long id) {
        return repository.save(ARole.builder().id(id).build());
    }
}
