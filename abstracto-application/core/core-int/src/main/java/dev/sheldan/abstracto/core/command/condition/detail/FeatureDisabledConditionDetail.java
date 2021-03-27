package dev.sheldan.abstracto.core.command.condition.detail;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;
import dev.sheldan.abstracto.core.command.model.condition.FeatureDisabledConditionDetailModel;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureDisabledConditionDetail implements ConditionDetail {

    private final FeatureDisabledConditionDetailModel model;

    public FeatureDisabledConditionDetail(FeatureConfig featureConfig) {
        this.model = FeatureDisabledConditionDetailModel.builder().featureConfig(featureConfig).build();
    }

    @Override
    public String getTemplateName() {
        return "feature_disabled_condition";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
