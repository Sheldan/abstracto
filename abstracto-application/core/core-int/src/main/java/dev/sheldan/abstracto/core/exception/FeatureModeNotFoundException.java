package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.FeatureModeNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

import java.util.List;

public class FeatureModeNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final FeatureModeNotFoundExceptionModel model;

    public FeatureModeNotFoundException(String featureMode, List<String> availableModes) {
        super("Feature mode not found.");
        this.model = FeatureModeNotFoundExceptionModel
                .builder()
                .availableModes(availableModes)
                .featureMode(featureMode)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "feature_mode_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
