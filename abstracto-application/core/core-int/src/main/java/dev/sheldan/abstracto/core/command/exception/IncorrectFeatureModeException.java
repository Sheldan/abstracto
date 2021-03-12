package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.exception.IncorrectFeatureModeModel;
import dev.sheldan.abstracto.core.templating.Templatable;

import java.util.List;

public class IncorrectFeatureModeException extends AbstractoRunTimeException implements Templatable {
    private final IncorrectFeatureModeModel model;

    public IncorrectFeatureModeException(FeatureDefinition featureDefinition, List<FeatureMode> requiredModes) {
        this.model = IncorrectFeatureModeModel.builder().featureDefinition(featureDefinition).requiredModes(requiredModes).build();
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
