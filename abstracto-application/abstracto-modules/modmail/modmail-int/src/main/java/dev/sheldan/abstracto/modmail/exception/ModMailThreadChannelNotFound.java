package dev.sheldan.abstracto.modmail.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class ModMailThreadChannelNotFound extends AbstractoRunTimeException {
    public ModMailThreadChannelNotFound() {
        super("Modmail thread channel not found.");
    }
}
