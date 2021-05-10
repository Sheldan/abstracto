package dev.sheldan.abstracto.invitefilter.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.sync.entity.ChannelGroupDeletedListener;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupDeletedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import dev.sheldan.abstracto.invitefilter.service.management.InviteFilterChannelGroupManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InviteFilterChannelGroupDeletedListener implements ChannelGroupDeletedListener {

    @Autowired
    private InviteFilterChannelGroupManagement inviteFilterChannelGroupManagement;

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Override
    public DefaultListenerResult execute(ChannelGroupDeletedListenerModel model) {
        AChannelGroup channelGroup = channelGroupManagementService.findChannelGroupById(model.getChannelGroupId());
        if(channelGroup.getChannelGroupType().getGroupTypeKey().equals(InviteLinkFilterService.INVITE_FILTER_CHANNEL_GROUP_TYPE)) {
            inviteFilterChannelGroupManagement.deleteInviteFilterChannelGroup(channelGroup);
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }
}
