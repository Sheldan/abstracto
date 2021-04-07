package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@Builder
public class SystemInfo {
    private Instant startTime;
    private Duration uptime;
}
