package dev.sheldan.abstracto.utility.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Builder
public class SuggestionNotFoundExceptionModel implements Serializable {
    private final Long suggestionId;
}
