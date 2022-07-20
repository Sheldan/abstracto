package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;
import dev.sheldan.abstracto.entertainment.model.exception.PayDayCooldownExceptionModel;

import java.time.Duration;

public class PayDayCooldownException extends AbstractoTemplatableException {

    private final PayDayCooldownExceptionModel model;

    public PayDayCooldownException(Duration duration) {
        this.model = PayDayCooldownExceptionModel
                .builder()
                .tryAgainDuration(duration)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "payday_cooldown_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
