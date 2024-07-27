package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Builder
public class InputFormatExceptionModel implements Serializable {
    private final String invalidFormat;
    private final String validFormat;
}
