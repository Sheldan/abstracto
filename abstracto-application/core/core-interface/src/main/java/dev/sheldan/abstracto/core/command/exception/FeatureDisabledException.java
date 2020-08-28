package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.command.models.exception.FeatureDisabledExceptionModel;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureDisabledException extends AbstractoRunTimeException implements Templatable {

    private final FeatureDisabledExceptionModel model;

    public FeatureDisabledException(FeatureConfig featureConfig) {
        super("Feature has been disabled");
        this.model = FeatureDisabledExceptionModel.builder().featureConfig(featureConfig).build();
    }

    @Override
    public String getTemplateName() {
        return "feature_disabled_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
