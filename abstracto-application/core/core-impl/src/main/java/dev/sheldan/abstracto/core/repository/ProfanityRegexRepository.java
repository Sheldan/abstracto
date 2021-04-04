package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfanityRegexRepository extends JpaRepository<ProfanityRegex, Long> {
    Optional<ProfanityRegex> findByGroupAndRegexNameIgnoreCase(ProfanityGroup group, String name);
    void deleteByGroupAndRegexNameIgnoreCase(ProfanityGroup group, String name);
}
