package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class FeatureModeNotFoundExceptionModel implements Serializable {
    private final String featureMode;
    private final List<String> availableModes;
}
