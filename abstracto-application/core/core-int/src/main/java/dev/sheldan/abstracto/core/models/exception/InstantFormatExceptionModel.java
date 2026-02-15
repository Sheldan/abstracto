package dev.sheldan.abstracto.core.models.exception;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class InstantFormatExceptionModel implements Serializable {
    private final String invalidFormat;
}
