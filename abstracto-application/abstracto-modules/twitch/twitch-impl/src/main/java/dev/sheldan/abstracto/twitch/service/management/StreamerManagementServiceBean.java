package dev.sheldan.abstracto.twitch.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.twitch.exception.StreamerExistsException;
import dev.sheldan.abstracto.twitch.model.database.Streamer;
import dev.sheldan.abstracto.twitch.repository.StreamerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class StreamerManagementServiceBean implements StreamerManagementService {

    @Autowired
    private StreamerRepository streamerRepository;

    @Override
    public Streamer createStreamer(String id, String name, AChannel targetChannel, AUserInAServer creator, AUserInAServer member, Boolean isOnline) {
        Streamer streamer = Streamer
                .builder()
                .name(name)
                .showNotifications(true)
                .userId(id)
                .online(isOnline)
                .creator(creator)
                .server(creator.getServerReference())
                .streamerUser(member)
                .notificationChannel(targetChannel)
                .build();
        log.info("Creating streamer {} because of user {} in server {}.", id, creator.getServerReference().getId(), creator.getUserReference().getId());
        return streamerRepository.save(streamer);
    }

    @Override
    public void removeStreamer(String id) {
        streamerRepository.deleteByUserId(id);
    }

    @Override
    public Optional<Streamer> getStreamerById(Long id) {
        return streamerRepository.findById(id);
    }

    @Override
    public void removeStreamer(Streamer streamer) {
        streamerRepository.delete(streamer);
    }

    @Override
    public void saveStreamer(Streamer streamer) {
        streamerRepository.save(streamer);
    }

    @Override
    public Streamer getStreamerInServerById(String id, Long serverId) {
        return streamerRepository.findByServer_IdAndUserId(serverId, id)
                .orElseThrow(StreamerExistsException::new);
    }

    @Override
    public List<Streamer> getStreamerInServers(String id) {
        return streamerRepository.getByUserId(id);
    }

    @Override
    public boolean streamerExistsInServerByID(String id, AServer server) {
        return streamerRepository.existsByServerAndUserId(server, id);
    }

    @Override
    public boolean streamerExistsInServerByName(String name, AServer server) {
        return streamerRepository.existsByServerAndName(server, name);
    }

    @Override
    public Optional<Streamer> getStreamerInServerByName(String name, AServer server) {
        return streamerRepository.findByServerAndName(server, name);
    }

    @Override
    public List<Streamer> getStreamersForServer(AServer server) {
        return streamerRepository.getStreamerByServer(server);
    }
}
