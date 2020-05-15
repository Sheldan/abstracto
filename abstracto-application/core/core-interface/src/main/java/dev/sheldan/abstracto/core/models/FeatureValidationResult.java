package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.templating.Templatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.GeneratedValue;
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
    private List<ValidationError> validationErrors = new ArrayList<>();

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
        params.put("featureTemplate", this.feature.getFeature().getKey() + "_feature");
        params.put("errors", this.validationErrors);
        return params;
    }
}
