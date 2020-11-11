package dev.sheldan.abstracto.statistic.emotes.repository;

import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.TrackedEmoteServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface TrackedEmoteRepository extends JpaRepository<TrackedEmote, TrackedEmoteServer> {
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<TrackedEmote> findByTrackedEmoteId_ServerIdAndDeletedFalseAndExternalFalse(Long serverId);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<TrackedEmote> findByTrackedEmoteId_ServerIdAndTrackingEnabledTrue(Long serverId);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<TrackedEmote> findByTrackedEmoteId_ServerId(Long serverId);
}
