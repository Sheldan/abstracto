package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ServerServiceBean implements ServerService {

    @Autowired
    private ServerRepository repository;

    @Override
    public AServer createServer(Long id) {
        return repository.save(AServer.builder().id(id).build());
    }

    @Override
    public void addChannelToServer(AServer server, AChannel channel) {
        server.getChannels().add(channel);
    }


}
