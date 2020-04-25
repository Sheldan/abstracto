package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.time.Instant;


@Getter
@SuperBuilder
@Setter
@NoArgsConstructor
public class UnMuteLog extends ServerContext {
    private Member unMutedUser;
    private Member mutingUser;
    private Mute mute;

    public Duration getMuteDuration() {
        return Duration.between(mute.getMuteDate(), Instant.now());
    }

    public Duration getPlannedMuteDuration() {
        return Duration.between(mute.getMuteDate(), mute.getMuteTargetDate());
    }

    public Instant getUnmuteDate() {
        return Instant.now();
    }

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(this.mute.getMutingServer().getId() ,this.getMute().getMutingChannel().getId(), this.mute.getMessageId());
    }
}
