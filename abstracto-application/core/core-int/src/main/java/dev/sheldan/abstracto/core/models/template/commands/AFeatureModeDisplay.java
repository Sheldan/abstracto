package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AFeatureModeDisplay {
    private String featureMode;
    private Boolean enabled;

    public static AFeatureModeDisplay fromFeatureMode(AFeatureMode aFeatureMode) {
        return AFeatureModeDisplay
                .builder()
                .featureMode(aFeatureMode.getFeatureMode())
                .enabled(aFeatureMode.getEnabled())
                .build();
    }
}
