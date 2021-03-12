package dev.sheldan.abstracto.repostdetection.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;

public class PostedImageNotFoundException extends AbstractoRunTimeException {

    public PostedImageNotFoundException(Long postedMessageId, Integer position) {
        super(String.format("Posted message with id %s and position %s was not found.", postedMessageId, position));
    }
}
