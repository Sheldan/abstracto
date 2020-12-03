package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.utility.models.database.RepostCheckChannelGroup;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public interface RepostCheckChannelService {
    void setRepostCheckEnabledForChannelGroup(AChannelGroup channelGroup);
    void setRepostCheckEnabledForChannelGroup(RepostCheckChannelGroup channelGroup);

    void setRepostCheckDisabledForChannelGroup(AChannelGroup channelGroup);
    void setRepostCheckDisabledForChannelGroup(RepostCheckChannelGroup channelGroup);

    boolean duplicateCheckEnabledForChannel(TextChannel textChannel);
    boolean duplicateCheckEnabledForChannel(AChannel channel);

    List<RepostCheckChannelGroup> getRepostCheckChannelGroupsForServer(AServer server);

    List<RepostCheckChannelGroup> getRepostCheckChannelGroupsForServer(Long serverId);

    List<RepostCheckChannelGroup> getChannelGroupsWithEnabledCheck(AServer server);
    List<RepostCheckChannelGroup> getChannelGroupsWithEnabledCheck(Long serverId);


}
