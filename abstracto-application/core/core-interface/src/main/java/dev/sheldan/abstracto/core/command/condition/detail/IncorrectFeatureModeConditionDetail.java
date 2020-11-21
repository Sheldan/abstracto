package dev.sheldan.abstracto.core.command.condition.detail;


import dev.sheldan.abstracto.core.command.condition.ConditionDetail;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.exception.IncorrectFeatureModeModel;

import java.util.List;

public class IncorrectFeatureModeConditionDetail implements ConditionDetail {

    private final IncorrectFeatureModeModel model;

    public IncorrectFeatureModeConditionDetail(FeatureEnum featureEnum, List<FeatureMode> requiredModes) {
        this.model = IncorrectFeatureModeModel.builder().featureEnum(featureEnum).requiredModes(requiredModes).build();
    }

    @Override
    public String getTemplateName() {
        return "incorrect_feature_mode_condition";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
