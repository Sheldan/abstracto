package dev.sheldan.abstracto.twitch.model.template;

import com.github.twitch4j.helix.domain.Stream;
import dev.sheldan.abstracto.twitch.model.database.StreamSessionSection;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class StreamSectionDisplay {
    private String gameName;
    private String gameId;
    private String title;
    private Integer viewerCount;
    private Instant startedAt;
    private String thumbnailURL;
    
    public static StreamSectionDisplay fromStream(Stream stream) {
        return StreamSectionDisplay
                .builder()
                .startedAt(stream.getStartedAtInstant())
                .gameName(stream.getGameName())
                .thumbnailURL(stream.getThumbnailUrl(1280, 720))
                .viewerCount(stream.getViewerCount())
                .title(stream.getTitle())
                .gameId(stream.getGameId())
                .build();
    }

    public static StreamSectionDisplay fromSection(StreamSessionSection section) {
        return StreamSectionDisplay
                .builder()
                .startedAt(section.getCreated())
                .gameName(section.getGameName())
                .viewerCount(section.getViewerCount())
                .title(section.getTitle())
                .gameId(section.getGameId())
                .build();
    }
}
