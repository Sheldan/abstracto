package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.FeatureModeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureModeManagementServiceBean implements FeatureModeManagementService {

    @Autowired
    private FeatureModeRepository featureModeRepository;

    @Override
    public AFeatureMode createMode(AFeatureFlag featureFlag, FeatureMode mode) {
        AFeatureMode aFeatureMode = AFeatureMode
                .builder()
                .featureFlag(featureFlag)
                .mode(mode.getKey())
                .build();

        featureModeRepository.save(aFeatureMode);
        return aFeatureMode;
    }

    @Override
    public AFeatureMode getModeForFeature(AFeatureFlag featureFlag) {
        return featureModeRepository.findByFeatureFlag(featureFlag);
    }

    @Override
    public boolean featureModeSet(AFeature aFeature, AServer server) {
        return featureModeRepository.existsByFeatureFlag_ServerAndFeatureFlag_Feature(server, aFeature);
    }

    @Override
    public AFeatureMode setModeForFeature(AFeatureFlag featureFlag, FeatureMode featureMode) {
        AFeatureMode modeForFeature = getModeForFeature(featureFlag);
        modeForFeature.setMode(featureMode.getKey());
        return modeForFeature;
    }
}
