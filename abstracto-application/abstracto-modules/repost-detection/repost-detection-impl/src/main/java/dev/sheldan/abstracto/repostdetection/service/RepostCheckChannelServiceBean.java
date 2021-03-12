package dev.sheldan.abstracto.repostdetection.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.repostdetection.service.management.RepostCheckChannelGroupManagement;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sheldan.abstracto.repostdetection.service.RepostServiceBean.REPOST_CHECK_CHANNEL_GROUP_TYPE;

@Component
public class RepostCheckChannelServiceBean implements RepostCheckChannelService {

    @Autowired
    private RepostCheckChannelGroupManagement repostCheckChannelManagement;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Override
    public void setRepostCheckEnabledForChannelGroup(AChannelGroup channelGroup) {
        RepostCheckChannelGroup group = repostCheckChannelManagement.loadRepostChannelGroupByChannelGroup(channelGroup);
        setRepostCheckEnabledForChannelGroup(group);
    }

    @Override
    public void setRepostCheckEnabledForChannelGroup(RepostCheckChannelGroup channelGroup) {
        channelGroup.setCheckEnabled(true);
    }

    @Override
    public void setRepostCheckDisabledForChannelGroup(AChannelGroup channelGroup) {
        RepostCheckChannelGroup group = repostCheckChannelManagement.loadRepostChannelGroupByChannelGroup(channelGroup);
        setRepostCheckDisabledForChannelGroup(group);
    }

    @Override
    public void setRepostCheckDisabledForChannelGroup(RepostCheckChannelGroup channelGroup) {
        channelGroup.setCheckEnabled(false);
    }

    @Override
    public boolean duplicateCheckEnabledForChannel(TextChannel textChannel) {
        AChannel channel = channelManagementService.loadChannel(textChannel.getIdLong());
        return duplicateCheckEnabledForChannel(channel);
    }

    @Override
    public boolean duplicateCheckEnabledForChannel(AChannel channel) {
        List<AChannelGroup> channelGroups = channelGroupService.getChannelGroupsOfChannelWithType(channel, REPOST_CHECK_CHANNEL_GROUP_TYPE);
        if(!channelGroups.isEmpty()) {
            List<RepostCheckChannelGroup> repostChannelGroups = channelGroups.stream().map(aChannelGroup ->
                repostCheckChannelManagement.loadRepostChannelGroupById(aChannelGroup.getId())
            ).collect(Collectors.toList());
            return repostChannelGroups.stream().anyMatch(RepostCheckChannelGroup::getCheckEnabled);
        }
        return false;
    }

    @Override
    public List<RepostCheckChannelGroup> getRepostCheckChannelGroupsForServer(AServer server) {
        return getRepostCheckChannelGroupsForServer(server.getId());
    }

    @Override
    public List<RepostCheckChannelGroup> getRepostCheckChannelGroupsForServer(Long serverId) {
        List<AChannelGroup> channelGroups = channelGroupManagementService.findAllInServerWithType(serverId, REPOST_CHECK_CHANNEL_GROUP_TYPE);
        if(!channelGroups.isEmpty()) {
            return channelGroups.stream().map(aChannelGroup ->
                    repostCheckChannelManagement.loadRepostChannelGroupById(aChannelGroup.getId())
            ).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<RepostCheckChannelGroup> getChannelGroupsWithEnabledCheck(AServer server) {
        return getChannelGroupsWithEnabledCheck(server.getId());
    }

    @Override
    public List<RepostCheckChannelGroup> getChannelGroupsWithEnabledCheck(Long serverId) {
        return getRepostCheckChannelGroupsForServer(serverId)
                .stream()
                .filter(RepostCheckChannelGroup::getCheckEnabled)
                .collect(Collectors.toList());
    }
}
