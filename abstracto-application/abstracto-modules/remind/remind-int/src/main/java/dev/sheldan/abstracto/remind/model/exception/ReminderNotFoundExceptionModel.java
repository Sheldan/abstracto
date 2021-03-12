package dev.sheldan.abstracto.remind.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class ReminderNotFoundExceptionModel implements Serializable {
    private final Long reminderId;
}
