package dev.sheldan.abstracto.core.models.config;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturePropertiesConfig {
    private Boolean enabled;
    private String defaultMode;
}
