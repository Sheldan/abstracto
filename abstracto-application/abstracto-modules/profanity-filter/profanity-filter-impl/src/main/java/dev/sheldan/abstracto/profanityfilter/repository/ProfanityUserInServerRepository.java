package dev.sheldan.abstracto.profanityfilter.repository;

import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfanityUserInServerRepository extends JpaRepository<ProfanityUserInAServer, Long> {
}
