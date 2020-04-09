package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Duration;

public interface SlowModeService {
    void setSlowMode(TextChannel channel, Duration duration);
    void setSlowMode(ChannelDto channel, Duration duration);
}
