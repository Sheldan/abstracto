package dev.sheldan.abstracto.suggestion.repository;

import dev.sheldan.abstracto.suggestion.model.database.PollUserDecisionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollUserDecisionOptionRepository extends JpaRepository<PollUserDecisionOption, Long> {
}
