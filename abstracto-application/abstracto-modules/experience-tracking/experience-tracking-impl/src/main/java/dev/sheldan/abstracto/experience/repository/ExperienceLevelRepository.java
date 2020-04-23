package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to manage the access to the table managed by {@link AExperienceLevel}
 */
@Repository
public interface ExperienceLevelRepository extends JpaRepository<AExperienceLevel, Integer> {
}
