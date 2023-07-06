package dev.sheldan.abstracto.twitch.service.management;

import com.github.twitch4j.helix.domain.Stream;
import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import dev.sheldan.abstracto.twitch.model.database.Streamer;

public interface StreamSessionManagementService {
    StreamSession startSession(Streamer streamer, Long messageId, Long channelId, Stream stream);
    void deleteSessionsOfStreamer(Streamer streamer);
    void deleteSession(StreamSession session);
    void deleteSession(Streamer streamer, Long messageId);
}
