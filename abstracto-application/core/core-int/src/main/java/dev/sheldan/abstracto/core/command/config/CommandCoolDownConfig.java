package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CommandCoolDownConfig {
    private Duration serverCoolDown;
    private Duration channelCoolDown;
    private Duration memberCoolDown;
}
