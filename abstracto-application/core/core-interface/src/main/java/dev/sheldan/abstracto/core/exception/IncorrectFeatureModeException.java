package dev.sheldan.abstracto.core.exception;


import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.exception.IncorrectFeatureModeExceptionModel;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.List;

public class IncorrectFeatureModeException extends AbstractoRunTimeException implements Templatable {

    private final IncorrectFeatureModeExceptionModel model;

    public IncorrectFeatureModeException(FeatureEnum featureEnum, List<FeatureMode> requiredModes) {
        super("Incorrect feature mode for the command.");
        this.model = IncorrectFeatureModeExceptionModel.builder().featureEnum(featureEnum).requiredModes(requiredModes).build();
    }

    @Override
    public String getTemplateName() {
        return "incorrect_feature_mode_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
