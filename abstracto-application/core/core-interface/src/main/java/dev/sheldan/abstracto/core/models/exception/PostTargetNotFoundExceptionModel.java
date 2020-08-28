package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Builder
public class PostTargetNotFoundExceptionModel implements Serializable {
    private final String postTargetKey;
}
