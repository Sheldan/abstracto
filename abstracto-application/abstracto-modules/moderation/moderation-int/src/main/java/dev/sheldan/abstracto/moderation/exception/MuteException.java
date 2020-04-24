package dev.sheldan.abstracto.moderation.exception;


import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class MuteException extends AbstractoRunTimeException {
    public MuteException(String message) {
        super(message);
    }
}

