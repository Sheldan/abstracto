package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class HoneyPotFeatureConfig implements FeatureConfig {

    public static final String HONEYPOT_ROLE_ID = "honeypotRoleId";

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.HONEYPOT;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(HONEYPOT_ROLE_ID);
    }
}
