package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public abstract class AbstractParameterHandlerTest {
    protected UnparsedCommandParameterPiece getPieceWithValue(String value) {
        UnparsedCommandParameterPiece mock = Mockito.mock(UnparsedCommandParameterPiece.class);
        when(mock.getValue()).thenReturn(value);
        return mock;
    }

    protected UnparsedCommandParameterPiece getPiece() {
        return Mockito.mock(UnparsedCommandParameterPiece.class);
    }
}
