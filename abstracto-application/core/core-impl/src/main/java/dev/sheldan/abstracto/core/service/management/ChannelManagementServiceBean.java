package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.repository.ChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChannelManagementServiceBean implements ChannelManagementService {

    @Autowired
    private ChannelRepository repository;

    @Override
    public AChannel loadChannel(Long id) {
        return repository.getOne(id);
    }

    @Override
    public AChannel createChannel(Long id, AChannelType type) {
        log.info("Creating channel {} with type {}", id, type);
        return repository.save(AChannel.builder().id(id).type(type).deleted(false).build());
    }

    @Override
    public void markAsDeleted(Long id) {
        AChannel channel =  loadChannel(id);
        channel.setDeleted(true);
        repository.save(channel);
    }

    @Override
    public void removeChannel(Long id) {
        log.info("Deleting channel {}", id);
        repository.deleteById(id);
    }
}
