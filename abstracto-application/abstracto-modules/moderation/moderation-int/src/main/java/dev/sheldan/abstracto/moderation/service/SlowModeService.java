package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Duration;

public interface SlowModeService {
    void setSlowMode(TextChannel channel, Duration duration);
    void setSlowMode(AChannel channel, Duration duration);
}
