package dev.sheldan.abstracto.experience.repository;

import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Repository to manage the access to the table managed by {@link AExperienceLevel}
 */
@Repository
public interface ExperienceLevelRepository extends JpaRepository<AExperienceLevel, Integer> {
    @NotNull
    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<AExperienceLevel> findById(@NonNull Integer aLong);

    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsById(@NonNull Integer aLong);

    @NotNull
    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<AExperienceLevel> findAll();
}
