package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ModerationFeatureConfig implements FeatureConfig {

    public static final String BAN_INFRACTION_POINTS = "banInfractionPoints";
    public static final String KICK_INFRACTION_POINTS = "kickInfractionPoints";

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(ModerationPostTarget.BAN_LOG, ModerationPostTarget.KICK_LOG, ModerationPostTarget.UN_BAN_LOG);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(KICK_INFRACTION_POINTS, BAN_INFRACTION_POINTS);
    }
}
