package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
public class UptimeModel extends SlimUserInitiatedServerContext {
    private Instant startDate;
    private Duration uptime;
}
