package dev.sheldan.abstracto.modmail.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ModMailCategoryIdExceptionModel implements Serializable {
    private final Long categoryId;
}
