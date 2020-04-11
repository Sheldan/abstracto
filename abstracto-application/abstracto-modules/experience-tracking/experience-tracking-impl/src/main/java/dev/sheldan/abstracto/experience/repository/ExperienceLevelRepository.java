package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperienceLevelRepository extends JpaRepository<AExperienceLevel, Integer> {
    AExperienceLevel findTopByExperienceNeededGreaterThanOrderByExperienceNeededAsc(Long experience);
}
