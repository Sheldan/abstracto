package dev.sheldan.abstracto.repostdetection.service.management;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.repostdetection.exception.RepostCheckChannelGroupNotFoundException;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.repostdetection.repository.RepostCheckChannelGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RepostCheckChannelGroupManagementBean implements RepostCheckChannelGroupManagement {

    @Autowired
    private RepostCheckChannelGroupRepository repository;

    @Override
    public RepostCheckChannelGroup loadRepostChannelGroupById(Long channelGroupId) {
        return loadRepostChannelGroupByIdOptional(channelGroupId).orElseThrow(() -> new RepostCheckChannelGroupNotFoundException(channelGroupId));
    }

    @Override
    public Optional<RepostCheckChannelGroup> loadRepostChannelGroupByIdOptional(Long channelGroupId) {
        return repository.findById(channelGroupId);
    }

    @Override
    public boolean repostCheckChannelGroupExists(Long channelGroupId) {
        return loadRepostChannelGroupByIdOptional(channelGroupId).isPresent();
    }

    @Override
    public Optional<RepostCheckChannelGroup> loadRepostChannelGroupByChannelGroupOptional(AChannelGroup channelGroup) {
        return loadRepostChannelGroupByIdOptional(channelGroup.getId());
    }

    @Override
    public RepostCheckChannelGroup loadRepostChannelGroupByChannelGroup(AChannelGroup channelGroup) {
        return loadRepostChannelGroupById(channelGroup.getId());
    }

    @Override
    public RepostCheckChannelGroup createRepostCheckChannelGroup(AChannelGroup channelGroup) {
        RepostCheckChannelGroup repostCheckChannelGroup = RepostCheckChannelGroup
                .builder()
                .checkEnabled(true)
                .channelGroup(channelGroup)
                .id(channelGroup.getId())
                .build();

        return repository.save(repostCheckChannelGroup);
    }

    @Override
    public void deleteRepostCheckChannelGroup(AChannelGroup channelGroup) {
        RepostCheckChannelGroup repostCheckChannelGroup = loadRepostChannelGroupByChannelGroup(channelGroup);
        repository.delete(repostCheckChannelGroup);
    }
}
