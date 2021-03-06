package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class CategoryNotFoundExceptionModel implements Serializable {
    private final Long categoryId;
    private final Long guildId;
}
