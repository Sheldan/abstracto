package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureJobTriggerRepository extends JpaRepository<AFeatureJobTrigger, Long> {
    List<AFeatureJobTrigger> findByFeature(AFeatureFlag featureFlag);
}
