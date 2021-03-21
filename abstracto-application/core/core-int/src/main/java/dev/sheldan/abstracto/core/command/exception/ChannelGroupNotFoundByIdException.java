package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class ChannelGroupNotFoundByIdException extends AbstractoRunTimeException {

    public ChannelGroupNotFoundByIdException() {
        super("Channel group not found by ID");
    }
}
