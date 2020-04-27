package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class FeatureNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final String feature;
    private final List<String> availableFeatures;

    public FeatureNotFoundException(String message, String feature, List<String> availableFeatures) {
        super(message);
        this.feature = feature;
        this.availableFeatures = availableFeatures;
    }

    @Override
    public String getTemplateName() {
        return "feature_not_found";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("featureName", this.feature);
        parameters.put("availableFeatures", this.availableFeatures);
        return parameters;
    }
}
