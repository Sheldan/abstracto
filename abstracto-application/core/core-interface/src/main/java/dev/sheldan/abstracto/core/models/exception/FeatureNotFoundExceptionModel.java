package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class FeatureNotFoundExceptionModel implements Serializable {
    private final String featureName;
    private final List<String> availableFeatures;
}
