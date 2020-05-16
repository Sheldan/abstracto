package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureModeRepository extends JpaRepository<AFeatureMode, Long> {
    AFeatureMode findByFeatureFlag(AFeatureFlag featureFlag);
    boolean existsByFeatureFlag_ServerAndFeatureFlag_Feature(AServer server, AFeature feature);
}
