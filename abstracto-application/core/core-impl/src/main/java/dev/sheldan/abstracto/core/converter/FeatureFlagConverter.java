package dev.sheldan.abstracto.core.converter;

import dev.sheldan.abstracto.core.config.FeatureDisplay;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.template.commands.FeatureFlagDisplay;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeatureFlagConverter {

    @Autowired
    private FeatureFlagService featureFlagService;

    public FeatureFlagDisplay fromAFeatureFlag(AFeatureFlag featureFlag) {
        FeatureEnum featureEnum = featureFlagService.getFeatureEnum(featureFlag.getFeature().getKey());
        FeatureDisplay forFeature = featureFlagService.getFeatureDisplayforFeature(featureEnum);
        return FeatureFlagDisplay
                .builder()
                .featureDisplay(forFeature)
                .featureFlag(featureFlag)
                .build();
    }

    public List<FeatureFlagDisplay> fromFeatureFlags(List<AFeatureFlag> featureFlags) {
        return featureFlags.stream().map(this::fromAFeatureFlag).collect(Collectors.toList());
    }
}
