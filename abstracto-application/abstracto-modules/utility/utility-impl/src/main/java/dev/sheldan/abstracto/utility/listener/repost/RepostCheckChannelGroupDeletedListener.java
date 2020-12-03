package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.listener.entity.ChannelGroupDeletedListener;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.utility.service.RepostServiceBean;
import dev.sheldan.abstracto.utility.service.management.RepostCheckChannelGroupManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepostCheckChannelGroupDeletedListener implements ChannelGroupDeletedListener {

    @Autowired
    private RepostCheckChannelGroupManagement checkChannelGroupManagement;

    @Override
    public void channelGroupDeleted(AChannelGroup channelGroup) {
        if(channelGroup.getChannelGroupType().getGroupTypeKey().equals(RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE)) {
            checkChannelGroupManagement.deleteRepostCheckChannelGroup(channelGroup);
        }
    }
}
