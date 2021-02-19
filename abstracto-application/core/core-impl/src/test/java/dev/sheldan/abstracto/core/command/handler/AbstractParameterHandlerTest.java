package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;

public abstract class AbstractParameterHandlerTest {
    protected UnparsedCommandParameterPiece getPieceWithValue(String value) {
        return UnparsedCommandParameterPiece.builder().value(value).build();
    }
}
