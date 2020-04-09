package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.repository.ChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChannelManagementServiceBean {

    @Autowired
    private ChannelRepository repository;

    @Autowired
    private ChannelConverter channelConverter;

    public ChannelDto loadChannel(Long id) {
        return channelConverter.fromChannel(repository.getOne(id));
    }

    public ChannelDto createChannel(Long id, AChannelType type) {
        log.info("Creating channel {} with type {}", id, type);
        AChannel save = repository.save(AChannel.builder().id(id).type(type).deleted(false).build());
        return channelConverter.fromChannel(save);
    }

    public void markAsDeleted(Long id) {
        ChannelDto channel =  loadChannel(id);
        channel.setDeleted(true);
        repository.save(channelConverter.fromDto(channel));
    }

    public void removeChannel(Long id) {
        log.info("Deleting channel {}", id);
        repository.deleteById(id);
    }
}
