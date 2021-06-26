package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public abstract class AbstractoTemplatableException extends AbstractoRunTimeException implements Templatable {
    public AbstractoTemplatableException(String message) {
        super(message);
    }

    public AbstractoTemplatableException() {
    }

    public AbstractoTemplatableException(Throwable throwable) {
        super(throwable);
    }

    public AbstractoTemplatableException(String message, Throwable cause) {
        super(message, cause);
    }
}
