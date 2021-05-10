package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.invitefilter.model.database.InviteFilterChannelGroup;

import java.util.Optional;

public interface InviteFilterChannelGroupManagement {
    InviteFilterChannelGroup loadInviteFilterChannelGroupById(Long channelGroupId);
    Optional<InviteFilterChannelGroup> loadInviteFilterChannelGroupByIdOptional(Long channelGroupId);
    boolean inviteFilterChannelGroupExists(Long channelGroupId);
    Optional<InviteFilterChannelGroup> loadInviteFilterChannelGroupByChannelGroupOptional(AChannelGroup channelGroup);
    InviteFilterChannelGroup loadInviteFilterChannelGroupByChannelGroup(AChannelGroup channelGroup);
    InviteFilterChannelGroup createInviteFilterChannelGroup(AChannelGroup channelGroup);
    void deleteInviteFilterChannelGroup(AChannelGroup channelGroup);
}
