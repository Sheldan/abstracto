package dev.sheldan.abstracto.twitch.service.management;

import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import dev.sheldan.abstracto.twitch.model.database.StreamSessionSection;
import dev.sheldan.abstracto.twitch.model.database.Streamer;

import java.time.Instant;

public interface StreamSessionSectionManagementService {
    StreamSessionSection addSection(StreamSession session, String gameId, String gameName, Instant startTime, String title, Integer viewerCount);
    void deleteSectionsOfSession(StreamSession session);
    void deleteSectionsOfStreamer(Streamer streamer);
}
