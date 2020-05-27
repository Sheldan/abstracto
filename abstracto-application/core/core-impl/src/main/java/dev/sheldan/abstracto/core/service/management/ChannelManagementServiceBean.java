package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.models.TableLocks;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.ChannelRepository;
import dev.sheldan.abstracto.core.service.LockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ChannelManagementServiceBean implements ChannelManagementService {

    @Autowired
    private ChannelRepository repository;

    @Autowired
    private LockService lockService;

    @Override
    public Optional<AChannel> loadChannel(Long id) {
        return repository.findById(id);
    }

    @Override
    public AChannel createChannel(Long id, AChannelType type, AServer server) {
        lockService.lockTable(TableLocks.CHANNELS);
        if(!channelExists(id)) {
            log.info("Creating channel {} with type {}", id, type);
            AChannel build = AChannel
                    .builder()
                    .id(id)
                    .type(type)
                    .server(server)
                    .deleted(false)
                    .build();
            return repository.save(build);
        } else {
            Optional<AChannel> channelOptional = loadChannel(id);
            return channelOptional.orElse(null);
        }
    }

    @Override
    public AChannel markAsDeleted(Long id) {
        Optional<AChannel> channelOptional = loadChannel(id);
        AChannel channel = channelOptional.orElseThrow(() -> new ChannelNotFoundException(id, 0L));
        channel.setDeleted(true);
        return channel;
    }

    @Override
    public boolean channelExists(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void removeChannel(Long id) {
        log.info("Deleting channel {}", id);
        repository.deleteById(id);
    }
}
