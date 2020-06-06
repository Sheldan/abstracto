package dev.sheldan.abstracto.utility.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class CrossServerEmbedException extends AbstractoRunTimeException {
    public CrossServerEmbedException(String message) {
        super(message);
    }
}
