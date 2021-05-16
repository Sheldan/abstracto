package dev.sheldan.abstracto.profanityfilter.repository;

import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUse;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfanityUseRepository extends JpaRepository<ProfanityUse, Long> {
    Long countByProfanityUserAndVerifiedTrueAndConfirmedTrue(ProfanityUserInAServer profanityUserInAServer);
    Long countByProfanityUserAndVerifiedTrueAndConfirmedFalse(ProfanityUserInAServer profanityUserInAServer);
    List<ProfanityUse> findAllByProfanityUserAndConfirmedTrueOrderByCreatedDesc(ProfanityUserInAServer profanityUserInAServer, Pageable pageable);
}
