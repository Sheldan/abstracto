package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Builder
public class DurationFormatExceptionModel implements Serializable {
    private final String invalidFormat;
    private final List<String> validFormats;
}
