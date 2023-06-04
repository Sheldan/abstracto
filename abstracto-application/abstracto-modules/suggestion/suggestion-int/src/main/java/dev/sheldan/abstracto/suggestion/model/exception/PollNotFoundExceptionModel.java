package dev.sheldan.abstracto.suggestion.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class PollNotFoundExceptionModel implements Serializable {
    private final Long pollId;
}
