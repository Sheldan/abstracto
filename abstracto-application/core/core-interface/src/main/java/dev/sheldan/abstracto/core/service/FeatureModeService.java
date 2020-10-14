package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;

import java.util.List;

public interface FeatureModeService {

    void enableFeatureModeForFeature(FeatureEnum featureEnum, AServer server, FeatureMode mode);
    void setFutureModeForFuture(FeatureEnum featureEnum, AServer server, FeatureMode mode, Boolean newValue);
    void disableFeatureModeForFeature(FeatureEnum featureEnum, AServer server, FeatureMode mode);
    boolean featureModeActive(FeatureEnum featureEnum, AServer server, FeatureMode mode);
    FeatureMode getFeatureModeForKey(String key);
    List<FeatureMode> getAllAvailableFeatureModes();
    List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server);
    List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server, AFeature feature);
}
