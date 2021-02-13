package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepository extends JpaRepository<AFeatureFlag, Long> {

    Optional<AFeatureFlag> findByServerAndFeature(AServer server, AFeature key);

    boolean existsByServerAndFeature(AServer server, AFeature key);

    List<AFeatureFlag> findAllByServer(AServer server);
}
