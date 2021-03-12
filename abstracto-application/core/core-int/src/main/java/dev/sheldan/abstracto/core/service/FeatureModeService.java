package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;

import java.util.List;

public interface FeatureModeService {

    void enableFeatureModeForFeature(FeatureDefinition featureDefinition, AServer server, FeatureMode mode);
    void setFutureModeForFuture(FeatureDefinition featureDefinition, AServer server, FeatureMode mode, Boolean newValue);
    void disableFeatureModeForFeature(FeatureDefinition featureDefinition, AServer server, FeatureMode mode);
    boolean featureModeActive(FeatureDefinition featureDefinition, AServer server, FeatureMode mode);
    boolean featureModeActive(FeatureDefinition featureDefinition, Long serverId, FeatureMode mode);
    void validateActiveFeatureMode(Long serverId, FeatureDefinition featureDefinition, FeatureMode mode);
    FeatureMode getFeatureModeForKey(String key);
    List<FeatureMode> getAllAvailableFeatureModes();
    List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server);
    List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server, AFeature feature);
    boolean necessaryFeatureModesMet(FeatureDefinition featureDefinition, List<FeatureMode> featureModes, Long serverId);
    boolean necessaryFeatureModesMet(FeatureAware featureAware, Long serverId);
}
