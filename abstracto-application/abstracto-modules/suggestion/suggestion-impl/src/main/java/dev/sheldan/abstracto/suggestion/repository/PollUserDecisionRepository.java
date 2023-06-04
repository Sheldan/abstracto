package dev.sheldan.abstracto.suggestion.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollUserDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollUserDecisionRepository extends JpaRepository<PollUserDecision, Long> {
    Optional<PollUserDecision> findPollUserDecisionByPollAndVoter(Poll poll, AUserInAServer voter);
}
