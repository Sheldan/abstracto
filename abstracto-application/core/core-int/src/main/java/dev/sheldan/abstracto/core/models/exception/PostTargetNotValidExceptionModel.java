package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class PostTargetNotValidExceptionModel implements Serializable {
    private final String postTargetKey;
    private final List<String> availableTargets;
}
