package dev.sheldan.abstracto.entertainment.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;
import dev.sheldan.abstracto.entertainment.model.exception.PayDayCooldownExceptionModel;
import dev.sheldan.abstracto.entertainment.model.exception.SlotsCooldownExceptionModel;

import java.time.Duration;

public class SlotsCooldownException extends AbstractoTemplatableException {

    private final SlotsCooldownExceptionModel model;

    public SlotsCooldownException(Duration duration) {
        this.model = SlotsCooldownExceptionModel
                .builder()
                .tryAgainDuration(duration)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "slots_cooldown_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
