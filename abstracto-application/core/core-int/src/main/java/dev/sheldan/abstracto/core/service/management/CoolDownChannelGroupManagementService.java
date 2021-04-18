package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.model.database.CoolDownChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;

public interface CoolDownChannelGroupManagementService {
    CoolDownChannelGroup createCoolDownChannelGroup(AChannelGroup aChannelGroup);
    CoolDownChannelGroup findByChannelGroupId(Long channelGroupId);
    void deleteCoolDownChannelGroup(AChannelGroup aChannelGroup);
}
