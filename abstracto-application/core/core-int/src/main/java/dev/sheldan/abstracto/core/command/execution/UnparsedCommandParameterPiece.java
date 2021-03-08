package dev.sheldan.abstracto.core.command.execution;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UnparsedCommandParameterPiece {
    private Object value;
    @Builder.Default
    private ParameterPieceType type = ParameterPieceType.STRING;
}
