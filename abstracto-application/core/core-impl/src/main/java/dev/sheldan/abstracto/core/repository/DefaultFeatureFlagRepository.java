package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.DefaultFeatureFlag;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface DefaultFeatureFlagRepository extends JpaRepository<DefaultFeatureFlag, Long> {
    @NotNull
    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<DefaultFeatureFlag> findAll();
}
