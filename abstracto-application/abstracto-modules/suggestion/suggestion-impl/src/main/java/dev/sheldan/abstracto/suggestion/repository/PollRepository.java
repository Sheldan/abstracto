package dev.sheldan.abstracto.suggestion.repository;

import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    Optional<Poll> findByPollIdAndServer_IdAndType(Long pollId, Long serverId, PollType pollType);
}
