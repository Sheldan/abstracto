package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.model.TableLocks;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.AChannelCreatedListenerModel;
import dev.sheldan.abstracto.core.models.listener.AChannelDeletedListenerModel;
import dev.sheldan.abstracto.core.repository.ChannelRepository;
import dev.sheldan.abstracto.core.service.LockService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ChannelManagementServiceBean implements ChannelManagementService {

    @Autowired
    private ChannelRepository repository;

    @Autowired
    private LockService lockService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<AChannel> loadChannelOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public AChannel loadChannel(Long id) {
        return loadChannelOptional(id).orElseThrow(() -> new ChannelNotFoundException(id));
    }

    @Override
    public AChannel loadChannel(Channel guildChannel) {
        return loadChannel(guildChannel.getIdLong());
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
            AChannel createdChannel = repository.save(build);
            AChannelCreatedListenerModel model = getCreationModel(createdChannel);
            eventPublisher.publishEvent(model);
            return createdChannel;
        } else {
            Optional<AChannel> channelOptional = loadChannelOptional(id);
            return channelOptional.orElse(null);
        }
    }

    @Override
    public AChannel createThread(Long id, AChannelType type, AServer server, AChannel parentChannel) {
        lockService.lockTable(TableLocks.CHANNELS);
        if(!channelExists(id)) {
            log.info("Creating channel {} with type {}", id, type);
            AChannel build = AChannel
                    .builder()
                    .id(id)
                    .type(type)
                    .relatedChannel(parentChannel)
                    .server(server)
                    .deleted(false)
                    .build();
            AChannel createdChannel = repository.save(build);
            AChannelCreatedListenerModel model = getCreationModel(createdChannel);
            eventPublisher.publishEvent(model);
            return createdChannel;
        } else {
            Optional<AChannel> channelOptional = loadChannelOptional(id);
            return channelOptional.orElse(null);
        }
    }

    private AChannelCreatedListenerModel getCreationModel(AChannel channel) {
        return AChannelCreatedListenerModel
                .builder()
                .channelId(channel.getId())
                .build();
    }

    private AChannelDeletedListenerModel getDeletionModel(AChannel channel) {
        return AChannelDeletedListenerModel
                .builder()
                .channelId(channel.getId())
                .build();
    }


    @Override
    public AChannel markAsDeleted(Long id) {
        AChannel channel = loadChannel(id);
        channel.setDeleted(true);
        AChannelDeletedListenerModel model = getDeletionModel(channel);
        eventPublisher.publishEvent(model);
        log.info("Marking channel {} as deleted.", id);
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
