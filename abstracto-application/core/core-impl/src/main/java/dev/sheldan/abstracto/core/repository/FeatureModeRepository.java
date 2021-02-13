package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureModeRepository extends JpaRepository<AFeatureMode, Long> {

    Optional<AFeatureMode> findByServerAndFeatureFlag_FeatureAndFeatureMode(AServer server, AFeature feature, String mode);

    List<AFeatureMode> findByServer(AServer server);

    List<AFeatureMode> findByServerAndFeatureFlag_Feature(AServer server, AFeature feature);

}
