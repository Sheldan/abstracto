package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureFlagRepository extends JpaRepository<AFeatureFlag, Long> {
    AFeatureFlag findByServerAndFeature(AServer server, AFeature key);
    List<AFeatureFlag> findAllByServer(AServer server);
}
