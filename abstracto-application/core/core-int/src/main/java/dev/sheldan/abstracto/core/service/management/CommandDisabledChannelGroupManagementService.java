package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.model.database.CommandDisabledChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;

public interface CommandDisabledChannelGroupManagementService {
    CommandDisabledChannelGroup createCommandDisabledChannelGroup(AChannelGroup channelGroup);
    void deleteCommandDisabledChannelGroup(AChannelGroup channelGroup);
    CommandDisabledChannelGroup findViaChannelGroup(AChannelGroup channelGroup);
}
