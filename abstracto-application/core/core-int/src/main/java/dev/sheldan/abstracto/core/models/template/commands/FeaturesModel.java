package dev.sheldan.abstracto.core.models.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@Setter
public class FeaturesModel {
    private List<FeatureFlagDisplay> features;
    private List<DefaultFeatureFlagDisplay> defaultFeatures;
}
