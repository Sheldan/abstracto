package dev.sheldan.abstracto.stickyroles.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class StickyRolesFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return StickyRolesFeatureDefinition.STICKY_ROLES;
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(StickyRoleFeatureMode.ALLOW_SELF_MANAGEMENT);
    }
}
