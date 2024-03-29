package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.FeatureNotFoundExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

import java.util.List;

public class FeatureNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final FeatureNotFoundExceptionModel model;

    public FeatureNotFoundException(String feature, List<String> availableFeatures) {
        super(String.format("Feature %s not found.", feature));
        this.model = FeatureNotFoundExceptionModel
                .builder()
                .featureName(feature)
                .availableFeatures(availableFeatures)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "feature_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
