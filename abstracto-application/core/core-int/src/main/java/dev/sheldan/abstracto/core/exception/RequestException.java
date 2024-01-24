package dev.sheldan.abstracto.core.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestException extends RuntimeException {
    private String errorMessage; // we just assume the body will be a plain text instead
    private Integer httpCode;
}
