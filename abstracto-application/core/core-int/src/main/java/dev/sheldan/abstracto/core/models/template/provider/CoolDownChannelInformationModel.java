package dev.sheldan.abstracto.core.models.template.provider;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
@Builder
public class CoolDownChannelInformationModel {
    private Duration channelCoolDown;
    private Duration memberCoolDown;
}
