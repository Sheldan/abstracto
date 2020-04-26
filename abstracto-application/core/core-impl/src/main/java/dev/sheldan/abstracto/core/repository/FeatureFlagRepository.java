package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureFlagRepository extends JpaRepository<AFeatureFlag, Long> {
    AFeatureFlag findByServerAndKey(AServer server, String key);
    List<AFeatureFlag> findAllByServer(AServer server);
}
