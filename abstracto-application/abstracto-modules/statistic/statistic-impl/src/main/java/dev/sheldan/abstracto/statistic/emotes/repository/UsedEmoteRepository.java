package dev.sheldan.abstracto.statistic.emotes.repository;

import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.UsedEmoteDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsedEmoteRepository extends JpaRepository<UsedEmote, UsedEmoteDay> {

    @Query(value="select * from used_emote " +
            "where emote_id = :emote_id and server_id = :server_id " +
            "and use_date = date_trunc('day', now())", nativeQuery = true)
    Optional<UsedEmote> findEmoteFromServerToday(@Param("emote_id") Long emoteId, @Param("server_id") Long server_id);

    @Query(value = "select us.emote_id as emoteId, us.server_id as serverId, sum(us.amount) as amount from used_emote us " +
            "inner join tracked_emote te " +
            "on us.emote_id = te.id and us.server_id = te.server_id " +
            "where us.use_date >= date_trunc('day', cast(:start_date AS timestamp)) and us.server_id = :server_id " +
            "group by us.emote_id, us.server_id " +
            "order by amount desc", nativeQuery = true)
    List<EmoteStatsResult> getEmoteStatsForServerSince(@Param("server_id") Long serverId, @Param("start_date") Instant since);

    @Query(value = "select us.emote_id as emoteId, us.server_id as serverId, sum(us.amount) as amount from used_emote us " +
            "inner join tracked_emote te " +
            "on us.emote_id = te.id and us.server_id = te.server_id " +
            "where us.use_date >= date_trunc('day', cast(:start_date AS timestamp)) and us.server_id = :server_id and te.external = true " +
            "group by us.emote_id, us.server_id " +
            "order by amount desc", nativeQuery = true)
    List<EmoteStatsResult> getExternalEmoteStatsForServerSince(@Param("server_id") Long serverId, @Param("start_date") Instant since);

    @Query(value = "select us.emote_id as emoteId, us.server_id as serverId, sum(us.amount) as amount from used_emote us " +
            "inner join tracked_emote te " +
            "on us.emote_id = te.id and us.server_id = te.server_id " +
            "where us.use_date >= date_trunc('day', cast(:start_date AS timestamp)) and us.server_id = :server_id and te.deleted = true " +
            "group by us.emote_id, us.server_id " +
            "order by amount desc", nativeQuery = true)
    List<EmoteStatsResult> getDeletedEmoteStatsForServerSince(@Param("server_id") Long serverId, @Param("start_date") Instant since);

    @Query(value = "select us.emote_id as emoteId, us.server_id as serverId, sum(us.amount) as amount from used_emote us " +
            "inner join tracked_emote te " +
            "on us.emote_id = te.id and us.server_id = te.server_id " +
            "where us.use_date >= date_trunc('day', cast(:start_date AS timestamp)) and us.server_id = :server_id and te.external = false and te.deleted = false " +
            "group by us.emote_id, us.server_id " +
            "order by amount desc", nativeQuery = true)
    List<EmoteStatsResult> getCurrentlyExistingEmoteStatsForServerSince(@Param("server_id") Long serverId, @Param("start_date") Instant since);

    void deleteByEmoteId_EmoteIdAndEmoteId_ServerIdAndEmoteId_UseDateGreaterThan(Long emoteId, Long serverId, Instant timestamp);

    List<UsedEmote> getByEmoteId_ServerIdAndEmoteId_UseDateGreaterThan(Long emoteId, Instant timestamp);
}
