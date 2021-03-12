package dev.sheldan.abstracto.repostdetection.service.management;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;

import java.util.Optional;

public interface RepostCheckChannelGroupManagement {
    RepostCheckChannelGroup loadRepostChannelGroupById(Long channelGroupId);
    Optional<RepostCheckChannelGroup> loadRepostChanelGroupByIdOptional(Long channelGroupId);
    boolean repostCheckChannelGroupExists(Long channelGroupId);
    Optional<RepostCheckChannelGroup> loadRepostChannelGroupByChannelGroupOptional(AChannelGroup channelGroup);
    RepostCheckChannelGroup loadRepostChannelGroupByChannelGroup(AChannelGroup channelGroup);
    RepostCheckChannelGroup createRepostCheckChannelGroup(AChannelGroup channelGroup);
    void deleteRepostCheckChannelGroup(AChannelGroup channelGroup);
}
