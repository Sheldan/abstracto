package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.experience.models.database.AUserExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExperienceRepository  extends JpaRepository<AUserExperience, Long> {
}
