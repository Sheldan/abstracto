package dev.sheldan.abstracto.twitch.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.twitch.model.database.Streamer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamerRepository extends JpaRepository<Streamer, Long> {
    boolean existsByServerAndUserId(AServer server, String userId);
    boolean existsByServerAndName(AServer server, String name);
    Optional<Streamer> findByServerAndName(AServer server, String name);
    Optional<Streamer> findByServer_IdAndUserId(Long serverId, String userId);
    List<Streamer> getByUserId(String userId);
    List<Streamer> getStreamerByServer(AServer server);
    void deleteByUserId(String id);
}
