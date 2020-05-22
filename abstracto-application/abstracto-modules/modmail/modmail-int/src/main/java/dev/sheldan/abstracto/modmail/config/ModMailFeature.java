package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import dev.sheldan.abstracto.modmail.validator.ModMailFeatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ModMailFeature implements FeatureConfig {

    @Autowired
    private ModMailFeatureValidator modMailFeatureValidator;

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(ModMailPostTargets.MOD_MAIL_PING, ModMailPostTargets.MOD_MAIL_LOG);
    }

    @Override
    public List<FeatureValidator> getAdditionalFeatureValidators() {
        return Arrays.asList(modMailFeatureValidator);
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("readReaction");
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(ModMailMode.LOGGING, ModMailMode.NO_LOG);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList("modMailClosingText");
    }
}
