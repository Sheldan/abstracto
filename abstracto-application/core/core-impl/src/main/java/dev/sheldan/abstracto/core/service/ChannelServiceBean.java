package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;
import dev.sheldan.abstracto.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelServiceBean implements ChannelService {

    @Autowired
    private ChannelRepository repository;

    @Override
    public AChannel loadChannel(Long id) {
        return repository.getOne(id);
    }

    @Override
    public AChannel createChannel(Long id, AChannelType type) {
        return repository.save(AChannel.builder().id(id).type(type).build());
    }
}
