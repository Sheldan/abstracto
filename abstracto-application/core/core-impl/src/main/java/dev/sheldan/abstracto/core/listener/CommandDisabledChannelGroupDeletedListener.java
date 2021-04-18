package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.listener.sync.entity.ChannelGroupDeletedListener;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupDeletedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.CommandDisabledChannelGroupManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.core.command.CommandConstants.COMMAND_CHANNEL_GROUP_KEY;

@Component
public class CommandDisabledChannelGroupDeletedListener implements ChannelGroupDeletedListener {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private CommandDisabledChannelGroupManagementService commandDisabledChannelGroupManagementService;

    @Override
    public DefaultListenerResult execute(ChannelGroupDeletedListenerModel model) {
        AChannelGroup channelGroup = channelGroupManagementService.findChannelGroupById(model.getChannelGroupId());
        if(channelGroup.getChannelGroupType().getGroupTypeKey().equals(COMMAND_CHANNEL_GROUP_KEY)) {
            commandDisabledChannelGroupManagementService.deleteCommandDisabledChannelGroup(channelGroup);
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }
}
