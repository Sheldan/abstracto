package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class FeatureModeNotFoundException extends AbstractoRunTimeException implements Templatable {

    private final String featureMode;
    private final List<String> availableModes;

    public FeatureModeNotFoundException(String message, String featureMode, List<String> availableModes) {
        super(message);
        this.featureMode = featureMode;
        this.availableModes = availableModes;
    }

    @Override
    public String getTemplateName() {
        return "feature_mode_not_found";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("featureMode", this.featureMode);
        parameters.put("availableModes", this.availableModes);
        return parameters;
    }
}
