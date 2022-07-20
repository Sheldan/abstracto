package dev.sheldan.abstracto.entertainment.model.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Builder
@Getter
public class SlotsCooldownExceptionModel {
    private Duration tryAgainDuration;
}
