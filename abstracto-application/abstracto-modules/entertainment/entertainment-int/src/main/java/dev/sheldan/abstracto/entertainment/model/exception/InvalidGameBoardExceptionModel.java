package dev.sheldan.abstracto.entertainment.model.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvalidGameBoardExceptionModel {
    private Double minMinesRatio;
}
