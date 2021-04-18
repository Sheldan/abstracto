package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.listener.sync.entity.AsyncChannelGroupCreatedListener;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupCreatedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.CommandDisabledChannelGroupManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.core.command.CommandConstants.COMMAND_CHANNEL_GROUP_KEY;

@Component
public class AsyncCommandDisabledChannelGroupCreatedListener implements AsyncChannelGroupCreatedListener {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;


    @Autowired
    private CommandDisabledChannelGroupManagementService commandDisabledChannelGroupManagementService;

    @Override
    public DefaultListenerResult execute(ChannelGroupCreatedListenerModel model) {
        AChannelGroup channelGroup = channelGroupManagementService.findChannelGroupById(model.getChannelGroupId());
        if(channelGroup.getChannelGroupType().getGroupTypeKey().equals(COMMAND_CHANNEL_GROUP_KEY)) {
            commandDisabledChannelGroupManagementService.createCommandDisabledChannelGroup(channelGroup);
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }
}
