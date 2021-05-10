package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.invitefilter.model.database.InviteFilterChannelGroup;
import dev.sheldan.abstracto.invitefilter.repository.InviteFilterChannelGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InviteFilterChannelGroupManagementBean implements InviteFilterChannelGroupManagement {

    @Autowired
    private InviteFilterChannelGroupRepository repository;

    @Override
    public InviteFilterChannelGroup loadInviteFilterChannelGroupById(Long channelGroupId) {
        return repository.findById(channelGroupId).orElseThrow(() -> new AbstractoRunTimeException("Invite filter channel group not found."));
    }

    @Override
    public Optional<InviteFilterChannelGroup> loadInviteFilterChannelGroupByIdOptional(Long channelGroupId) {
        return repository.findById(channelGroupId);
    }

    @Override
    public boolean inviteFilterChannelGroupExists(Long channelGroupId) {
        return loadInviteFilterChannelGroupByIdOptional(channelGroupId).isPresent();
    }

    @Override
    public Optional<InviteFilterChannelGroup> loadInviteFilterChannelGroupByChannelGroupOptional(AChannelGroup channelGroup) {
        return loadInviteFilterChannelGroupByChannelGroupOptional(channelGroup);
    }

    @Override
    public InviteFilterChannelGroup loadInviteFilterChannelGroupByChannelGroup(AChannelGroup channelGroup) {
        return loadInviteFilterChannelGroupById(channelGroup.getId());
    }

    @Override
    public InviteFilterChannelGroup createInviteFilterChannelGroup(AChannelGroup channelGroup) {
        InviteFilterChannelGroup group = InviteFilterChannelGroup
                .builder()
                .channelGroup(channelGroup)
                .id(channelGroup.getId())
                .build();
        return repository.save(group);
    }

    @Override
    public void deleteInviteFilterChannelGroup(AChannelGroup channelGroup) {
        InviteFilterChannelGroup foundGroup = loadInviteFilterChannelGroupByChannelGroup(channelGroup);
        repository.delete(foundGroup);
    }
}
