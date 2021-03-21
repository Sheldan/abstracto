package dev.sheldan.abstracto.repostdetection.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.sync.entity.AsyncChannelGroupCreatedListener;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupCreatedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.repostdetection.service.RepostServiceBean;
import dev.sheldan.abstracto.repostdetection.service.management.RepostCheckChannelGroupManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepostCheckAsyncChannelGroupCreatedListener implements AsyncChannelGroupCreatedListener {

    @Autowired
    private RepostCheckChannelGroupManagement checkChannelGroupManagement;

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Override
    public DefaultListenerResult execute(ChannelGroupCreatedListenerModel model) {
        AChannelGroup channelGroup = channelGroupManagementService.findChannelGroupById(model.getChannelGroupId());
        if(channelGroup.getChannelGroupType().getGroupTypeKey().equals(RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)) {
            checkChannelGroupManagement.createRepostCheckChannelGroup(channelGroup);
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }
}
