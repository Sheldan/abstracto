package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class HoneyPotFeatureConfig implements FeatureConfig {

    public static final String HONEYPOT_ROLE_ID = "honeypotRoleId";
    public static final String HONEYPOT_IGNORED_LEVEL = "honeypotIgnoredLevel";
    public static final String HONEYPOT_IGNORED_JOIN_DURATION_SECONDS = "honeypotIgnoredJoinDurationSeconds";

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.HONEYPOT;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(HONEYPOT_ROLE_ID, HONEYPOT_IGNORED_LEVEL, HONEYPOT_IGNORED_JOIN_DURATION_SECONDS);
    }
}
