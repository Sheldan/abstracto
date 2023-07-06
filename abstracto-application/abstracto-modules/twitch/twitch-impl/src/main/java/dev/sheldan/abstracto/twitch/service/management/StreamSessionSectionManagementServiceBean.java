package dev.sheldan.abstracto.twitch.service.management;

import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import dev.sheldan.abstracto.twitch.model.database.StreamSessionSection;
import dev.sheldan.abstracto.twitch.model.database.Streamer;
import dev.sheldan.abstracto.twitch.repository.StreamSessionSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class StreamSessionSectionManagementServiceBean implements StreamSessionSectionManagementService {

    @Autowired
    private StreamSessionSectionRepository repository;

    @Override
    public StreamSessionSection addSection(StreamSession session, String gameId, String gameName, Instant startTime, String title, Integer viewerCount) {
        StreamSessionSection section = StreamSessionSection
                .builder()
                .streamer(session.getStreamer())
                .gameId(gameId)
                .gameName(gameName)
                .title(title)
                .viewerCount(viewerCount)
                .session(session)
                .build();
        return repository.save(section);
    }

    @Override
    public void deleteSectionsOfSession(StreamSession session) {
        repository.deleteAll(session.getSections());
        session.getSections().clear();
    }

    @Override
    public void deleteSectionsOfStreamer(Streamer streamer) {
        List<StreamSessionSection> sections = streamer
                .getSessions()
                .stream()
                .flatMap(session -> session.getSections().stream())
                .toList();
        repository.deleteAll(sections);
        streamer.getSessions().forEach(session -> {
            session.getSections().clear();
        });
    }
}
