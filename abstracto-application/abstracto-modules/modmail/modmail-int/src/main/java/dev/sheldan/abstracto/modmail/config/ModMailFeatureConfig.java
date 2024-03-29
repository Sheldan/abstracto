package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.interactive.SetupStep;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import dev.sheldan.abstracto.modmail.setup.ModMailCategorySetup;
import dev.sheldan.abstracto.modmail.validator.ModMailFeatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * General instance of {@link FeatureConfig} to establish the mod mail feature
 */
@Component
public class ModMailFeatureConfig implements FeatureConfig {

    public static final String MOD_MAIL_CLOSING_TEXT_SYSTEM_CONFIG_KEY = "modMailClosingText";
    @Autowired
    private ModMailFeatureValidator modMailFeatureValidator;

    @Autowired
    private ModMailCategorySetup modMailCategorySetup;

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(ModMailPostTargets.MOD_MAIL_PING, ModMailPostTargets.MOD_MAIL_LOG, ModMailPostTargets.MOD_MAIL_CONTAINER);
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
        return Arrays.asList(ModMailMode.LOGGING, ModMailMode.SEPARATE_MESSAGE, ModMailMode.THREAD_CONTAINER);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(MOD_MAIL_CLOSING_TEXT_SYSTEM_CONFIG_KEY);
    }

    @Override
    public List<SetupStep> getCustomSetupSteps() {
        return Arrays.asList(modMailCategorySetup);
    }
}
