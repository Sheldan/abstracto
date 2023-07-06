package dev.sheldan.abstracto.twitch.service;

import com.github.twitch4j.helix.domain.Stream;
import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import dev.sheldan.abstracto.twitch.model.database.StreamSessionSection;
import dev.sheldan.abstracto.twitch.service.management.StreamSessionSectionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamSessionSectionServiceBean implements StreamSessionSectionService {

    @Autowired
    private StreamSessionSectionManagementService sessionSectionManagementService;

    @Override
    public StreamSessionSection createSectionFromStream(StreamSession session, Stream stream) {
        return sessionSectionManagementService.addSection(session, stream.getGameId(), stream.getGameName(),
                stream.getStartedAtInstant(), stream.getTitle(), stream.getViewerCount());
    }
}
