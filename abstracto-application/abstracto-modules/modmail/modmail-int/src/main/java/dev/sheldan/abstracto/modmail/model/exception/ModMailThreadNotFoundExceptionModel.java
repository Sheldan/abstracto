package dev.sheldan.abstracto.modmail.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ModMailThreadNotFoundExceptionModel implements Serializable {
    private final Long modMailThreadId;
}
