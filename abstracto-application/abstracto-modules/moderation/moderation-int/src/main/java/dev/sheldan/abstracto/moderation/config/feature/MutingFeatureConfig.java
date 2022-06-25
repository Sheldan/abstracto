package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.posttarget.MutingPostTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MutingFeatureConfig implements FeatureConfig {

    public static final String MUTE_INFRACTION_POINTS = "muteInfractionPoints";

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(MutingPostTarget.MUTE_LOG);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(MUTE_INFRACTION_POINTS);
    }
}
