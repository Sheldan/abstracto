package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.ChannelGroupTypeNotFound;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.repository.ChannelGroupTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ChannelGroupTypeManagementServiceBean implements ChannelGroupTypeManagementService {

    @Autowired
    private ChannelGroupTypeRepository repository;

    @Override
    public Optional<ChannelGroupType> findChannelGroupTypeByKeyOptional(String key) {
        return repository.findByGroupTypeKeyIgnoreCase(key);
    }

    @Override
    public ChannelGroupType findChannelGroupTypeByKey(String key) {
        return findChannelGroupTypeByKeyOptional(key).orElseThrow(() -> new ChannelGroupTypeNotFound(getAllChannelGroupTypesAsString()));
    }

    @Override
    public boolean doesChannelGroupTypeExist(String key) {
        return findChannelGroupTypeByKeyOptional(key).isPresent();
    }

    @Override
    public List<ChannelGroupType> getAllChannelGroupTypes() {
        return repository.findAll();
    }

    @Override
    public List<String> getAllChannelGroupTypesAsString() {
        return getAllChannelGroupTypes().stream().map(ChannelGroupType::getGroupTypeKey).collect(Collectors.toList());
    }
}
