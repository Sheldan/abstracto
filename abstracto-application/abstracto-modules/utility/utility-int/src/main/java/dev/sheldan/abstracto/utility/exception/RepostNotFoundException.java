package dev.sheldan.abstracto.utility.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class RepostNotFoundException extends AbstractoRunTimeException {
    public RepostNotFoundException(Long originalPostId, Integer originalPositionId, Long userInServerId) {
        super(String.format("Repost with image post id %s and position %s from user %s was not found.", originalPostId, originalPositionId, userInServerId));
    }
}
