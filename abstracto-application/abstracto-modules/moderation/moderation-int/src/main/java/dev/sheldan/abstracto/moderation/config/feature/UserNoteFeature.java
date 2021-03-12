package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

@Component
public class UserNoteFeature implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.USER_NOTES;
    }
}
