package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.CoolDownChannelGroup;
import dev.sheldan.abstracto.core.command.repository.CoolDownChannelGroupRepository;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.service.management.CoolDownChannelGroupManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoolDownChannelGroupManagementServiceBean implements CoolDownChannelGroupManagementService {

    @Autowired
    private CoolDownChannelGroupRepository repository;

    @Override
    public CoolDownChannelGroup createCoolDownChannelGroup(AChannelGroup aChannelGroup) {
        CoolDownChannelGroup newChannelGroup = CoolDownChannelGroup
                .builder()
                .channelGroup(aChannelGroup)
                .id(aChannelGroup.getId())
                .memberCoolDown(0L)
                .channelCoolDown(0L)
                .build();
        return repository.save(newChannelGroup);
    }

    @Override
    public CoolDownChannelGroup findByChannelGroupId(Long channelGroupId) {
        return repository.getOne(channelGroupId);
    }

    @Override
    public void deleteCoolDownChannelGroup(AChannelGroup aChannelGroup) {
        repository.delete(findByChannelGroupId(aChannelGroup.getId()));
    }
}
