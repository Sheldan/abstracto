package dev.sheldan.abstracto.core.models.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagProperty {
    private String featureName;
    private Boolean enabled;
}
