package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to manage the access to the table managed by {@link AExperienceLevel experienceLevel}
 */
@Repository
public interface ExperienceLevelRepository extends JpaRepository<AExperienceLevel, Integer> {

}
