package dev.sheldan.abstracto.twitch.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.twitch.model.database.Streamer;

import java.util.List;
import java.util.Optional;

public interface StreamerManagementService {
    Streamer createStreamer(String id, String name, AChannel targetChannel, AUserInAServer creator, AUserInAServer member, Boolean isOnline);
    void removeStreamer(String id);
    Optional<Streamer> getStreamerById(Long id);
    void removeStreamer(Streamer streamer);
    void saveStreamer(Streamer streamer);
    Streamer getStreamerInServerById(String id, Long serverId);
    List<Streamer> getStreamerInServers(String id);
    boolean streamerExistsInServerByID(String id, AServer server);
    boolean streamerExistsInServerByName(String name, AServer server);
    Optional<Streamer> getStreamerInServerByName(String name, AServer server);
    List<Streamer> getStreamersForServer(AServer server);
}
