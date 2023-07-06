package dev.sheldan.abstracto.twitch.service.management;

import com.github.twitch4j.helix.domain.Stream;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.twitch.model.database.Streamer;
import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import dev.sheldan.abstracto.twitch.repository.StreamSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamSessionManagementServiceBean implements StreamSessionManagementService {

    @Autowired
    private StreamSessionRepository repository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public StreamSession startSession(Streamer streamer, Long messageId, Long channelId, Stream stream) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        StreamSession notification = StreamSession
                .builder()
                .id(messageId)
                .startTime(stream.getStartedAtInstant())
                .channel(channel)
                .streamer(streamer)
                .streamId(stream.getId())
                .build();
        return repository.save(notification);
    }

    @Override
    public void deleteSessionsOfStreamer(Streamer streamer) {
        repository.deleteAll(streamer.getSessions());
        streamer.getSessions().clear();
    }

    @Override
    public void deleteSession(StreamSession notification) {
        notification.getStreamer().getSessions().remove(notification);
        repository.delete(notification);
    }

    @Override
    public void deleteSession(Streamer streamer, Long messageId) {
        streamer.getSessions().removeIf(notification -> notification.getId().equals(messageId));
        repository.deleteById(messageId);
    }
}
