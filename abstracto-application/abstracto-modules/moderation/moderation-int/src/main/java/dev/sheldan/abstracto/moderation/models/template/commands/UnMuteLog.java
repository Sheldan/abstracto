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

/**
 * Used when rendering the notification when a member was muted. The template is: "unmute_log_embed"
 */
@Getter
@SuperBuilder
@Setter
@NoArgsConstructor
public class UnMuteLog extends ServerContext {
    /**
     * The un-muted Member, is null if the member left the server
     */
    private Member unMutedUser;
    /**
     * The user casting the mute, is null if the member left the server
     */
    private Member mutingUser;
    /**
     * The persisted mute object from the database containing the information about the mute
     */
    private Mute mute;

    /**
     * The actual duration between the date the mute started and the current time
     * @return The difference between mute start and now
     */
    public Duration getMuteDuration() {
        return Duration.between(mute.getMuteDate(), Instant.now());
    }

    /**
     * The duration between the date the mute started and the un-mute planned
     * @return The difference between mute start and the target date
     */
    public Duration getPlannedMuteDuration() {
        return Duration.between(mute.getMuteDate(), mute.getMuteTargetDate());
    }

    /**
     * The un-mute date, which is now, because this is the un-mute log message.
     * @return The current time stamp
     */
    public Instant getUnmuteDate() {
        return Instant.now();
    }

    /**
     * Builds the link to the original message triggering the mute
     * @return A string containing an URL leading to the message where the mute was triggered
     */
    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(this.mute.getMutingServer().getId() ,this.getMute().getMutingChannel().getId(), this.mute.getMessageId());
    }
}
