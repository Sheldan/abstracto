package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ChannelGroupType;

import java.util.List;
import java.util.Optional;

public interface ChannelGroupTypeManagementService {
    Optional<ChannelGroupType> findChannelGroupTypeByKeyOptional(String key);
    ChannelGroupType findChannelGroupTypeByKey(String key);
    boolean doesChannelGroupTypeExist(String key);
    List<ChannelGroupType> getAllChannelGroupTypes();
    List<String> getAllChannelGroupTypesAsString();
}
