package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.listener.sync.entity.ChannelGroupCreatedListener;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.utility.service.RepostServiceBean;
import dev.sheldan.abstracto.utility.service.management.RepostCheckChannelGroupManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepostCheckChannelGroupCreatedListener implements ChannelGroupCreatedListener {

    @Autowired
    private RepostCheckChannelGroupManagement checkChannelGroupManagement;

    @Override
    public void channelGroupCreated(AChannelGroup channelGroup) {
        if(channelGroup.getChannelGroupType().getGroupTypeKey().equals(RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)) {
            checkChannelGroupManagement.createRepostCheckChannelGroup(channelGroup);
        }
    }
}
