package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.posttarget.InfractionPostTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class InfractionFeatureConfig implements FeatureConfig {

    public static final String INFRACTION_LEVELS = "infractionLevels";
    public static final String INFRACTION_LEVEL_PREFIX = "infractionLevel";

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.INFRACTIONS;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(INFRACTION_LEVELS);
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(InfractionPostTarget.INFRACTION_NOTIFICATION);
    }
}
