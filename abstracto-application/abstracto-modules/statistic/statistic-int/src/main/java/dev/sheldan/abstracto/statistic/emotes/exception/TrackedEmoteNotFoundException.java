package dev.sheldan.abstracto.statistic.emotes.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class TrackedEmoteNotFoundException extends AbstractoRunTimeException {

    public TrackedEmoteNotFoundException(String message) {
        super(message);
    }

    public TrackedEmoteNotFoundException() {
    }
}
