package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.DefaultPostTarget;
import dev.sheldan.abstracto.core.repository.DefaultPostTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultPostTargetManagementServiceBean implements DefaultPostTargetManagementService {

    @Autowired
    private DefaultPostTargetRepository repository;

    @Override
    public List<DefaultPostTarget> getAllDefaultPostTargets() {
        return repository.findAll();
    }

    @Override
    public List<String> getDefaultPostTargetKeys() {
        return getAllDefaultPostTargets().stream().map(DefaultPostTarget::getName).collect(Collectors.toList());
    }
}
