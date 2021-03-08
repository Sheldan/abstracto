package dev.sheldan.abstracto.core.interactive;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class SetupStepExceptionModel implements Serializable {
    private String templateKey;
    private transient Object templateModel;
    private String message;
}
