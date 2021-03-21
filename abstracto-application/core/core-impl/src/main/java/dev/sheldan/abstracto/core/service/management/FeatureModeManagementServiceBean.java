package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.repository.FeatureModeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FeatureModeManagementServiceBean implements FeatureModeManagementService {

    @Autowired
    private FeatureModeRepository featureModeRepository;

    @Autowired
    private DefaultFeatureModeManagement defaultFeatureModeManagement;

    @Override
    public AFeatureMode createMode(AFeatureFlag featureFlag, FeatureMode mode, boolean enabled) {
        return createMode(featureFlag, mode.getKey(), enabled);
    }

    @Override
    public AFeatureMode createMode(AFeatureFlag featureFlag, String mode, boolean enabled) {
        AFeatureMode aFeatureMode = AFeatureMode
                .builder()
                .featureFlag(featureFlag)
                .server(featureFlag.getServer())
                .enabled(enabled)
                .featureMode(mode)
                .build();

        return featureModeRepository.save(aFeatureMode);
    }

    @Override
    public boolean isFeatureModeActive(AFeature aFeature, AServer server, FeatureMode mode) {
        Optional<AFeatureMode> featureModeOptional = featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, aFeature, mode.getKey());
        return featureModeOptional.isPresent() && featureModeOptional.get().getEnabled();
    }

    @Override
    public boolean doesFeatureModeExist(AFeature aFeature, AServer server, FeatureMode mode) {
        return getFeatureMode(aFeature, server, mode).isPresent();
    }

    @Override
    public boolean doesFeatureModeExist(AFeature aFeature, AServer server, String modeKey) {
        return featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, aFeature, modeKey).isPresent();
    }

    @Override
    public Optional<AFeatureMode> getFeatureMode(AFeature aFeature, AServer server, FeatureMode mode) {
        return getFeatureMode(aFeature, server, mode.getKey());
    }

    @Override
    public Optional<AFeatureMode> getFeatureMode(AFeature aFeature, AServer server, String modeKey) {
        return featureModeRepository.findByServerAndFeatureFlag_FeatureAndFeatureMode(server, aFeature, modeKey);
    }

    @Override
    public List<AFeatureMode> getFeatureModesOfServer(AServer server) {
        return featureModeRepository.findByServer(server);
    }

    @Override
    public List<AFeatureMode> getFeatureModesOfFeatureInServer(AServer server, AFeature aFeature) {
        return featureModeRepository.findByServerAndFeatureFlag_Feature(server, aFeature);
    }


}
