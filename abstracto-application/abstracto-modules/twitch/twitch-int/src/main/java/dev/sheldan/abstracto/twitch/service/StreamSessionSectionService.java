package dev.sheldan.abstracto.twitch.service;

import com.github.twitch4j.helix.domain.Stream;
import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import dev.sheldan.abstracto.twitch.model.database.StreamSessionSection;

public interface StreamSessionSectionService {
    StreamSessionSection createSectionFromStream(StreamSession session, Stream stream);
}
