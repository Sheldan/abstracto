package dev.sheldan.abstracto.core.converter;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import dev.sheldan.abstracto.core.models.template.commands.DefaultFeatureFlagDisplay;
import dev.sheldan.abstracto.core.models.template.commands.FeatureFlagDisplay;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeatureFlagConverter {

    @Autowired
    private FeatureConfigService featureFlagService;

    public FeatureFlagDisplay fromAFeatureFlag(AFeatureFlag featureFlag) {
        FeatureDefinition featureDefinition = featureFlagService.getFeatureEnum(featureFlag.getFeature().getKey());
        FeatureConfig forFeature = featureFlagService.getFeatureDisplayForFeature(featureDefinition);
        return FeatureFlagDisplay
                .builder()
                .featureConfig(forFeature)
                .featureFlag(featureFlag)
                .build();
    }

    public DefaultFeatureFlagDisplay fromFeatureFlagProperty(FeatureFlagProperty featureFlagProperty) {
        FeatureDefinition featureDefinition = featureFlagService.getFeatureEnum(featureFlagProperty.getFeatureName());
        FeatureConfig forFeature = featureFlagService.getFeatureDisplayForFeature(featureDefinition);
        return DefaultFeatureFlagDisplay
                .builder()
                .featureConfig(forFeature)
                .featureFlagProperty(featureFlagProperty)
                .build();
    }

    public List<FeatureFlagDisplay> fromFeatureFlags(List<AFeatureFlag> featureFlags) {
        return featureFlags.stream().map(this::fromAFeatureFlag).collect(Collectors.toList());
    }

    public List<DefaultFeatureFlagDisplay> fromFeatureFlagProperties(List<FeatureFlagProperty> featureFlags) {
        return featureFlags.stream().map(this::fromFeatureFlagProperty).collect(Collectors.toList());
    }
}
