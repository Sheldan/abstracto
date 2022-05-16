package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.templating.Templatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Builder
@Getter
@Setter
public class FeatureValidationResult implements Templatable {

    private FeatureConfig feature;
    private Boolean validationResult;
    @Builder.Default
    private List<ValidationErrorModel> validationErrorModels = new ArrayList<>();
    private String validationText;

    public static FeatureValidationResult validationSuccessful(FeatureConfig featureConfig) {
        return FeatureValidationResult
                .builder()
                .feature(featureConfig)
                .validationResult(true)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "feature_not_setup_message";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("featureTemplate", "feature_" + this.feature.getFeature().getKey());
        params.put("errors", this.validationErrorModels);
        return params;
    }
}
