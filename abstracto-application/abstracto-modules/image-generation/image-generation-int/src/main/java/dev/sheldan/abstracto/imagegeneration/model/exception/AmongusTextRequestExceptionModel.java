package dev.sheldan.abstracto.imagegeneration.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class AmongusTextRequestExceptionModel implements Serializable {
    private String inputText;
    private String errorMessage;
}
