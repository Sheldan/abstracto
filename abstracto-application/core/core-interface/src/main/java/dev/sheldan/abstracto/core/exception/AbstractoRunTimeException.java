package dev.sheldan.abstracto.core.exception;

public class AbstractoRunTimeException extends RuntimeException {
    public AbstractoRunTimeException(String message) {
        super(message);
    }

    public AbstractoRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
