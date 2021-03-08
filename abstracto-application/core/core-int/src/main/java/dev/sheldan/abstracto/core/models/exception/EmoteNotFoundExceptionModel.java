package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class EmoteNotFoundExceptionModel implements Serializable {
    private final String emoteKey;
    private final List<String> available;
}
