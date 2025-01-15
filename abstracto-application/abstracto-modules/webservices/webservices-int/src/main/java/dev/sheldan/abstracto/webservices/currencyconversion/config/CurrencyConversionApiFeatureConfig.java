package dev.sheldan.abstracto.webservices.currencyconversion.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import org.springframework.stereotype.Component;

@Component
public class CurrencyConversionApiFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.CURRENCY_CONVERSION;
    }

}
