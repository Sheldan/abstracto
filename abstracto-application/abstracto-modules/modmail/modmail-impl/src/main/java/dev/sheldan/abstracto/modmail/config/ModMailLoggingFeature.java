package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ModMailLoggingFeature implements FeatureConfig {

    @Autowired
    private ModMailFeature modMailFeature;

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL_LOGGING;
    }

    @Override
    public List<FeatureConfig> getRequiredFeatures() {
        return Arrays.asList(modMailFeature);
    }
}
