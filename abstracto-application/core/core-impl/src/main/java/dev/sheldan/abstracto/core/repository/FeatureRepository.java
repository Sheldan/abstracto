package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

@Repository
public interface FeatureRepository extends JpaRepository<AFeature, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    AFeature findByKey(String key);
}
