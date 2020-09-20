package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.time.Duration;
import java.time.Instant;


/**
 * Used when rendering the notification when a member was muted. The template is: "mute_log_embed"
 */
@Getter
@SuperBuilder
@Setter
public class MuteContext {
    /**
     * The {@link Member} being muted
     */
    private Member mutedUser;
    /**
     * The {@link Member} executing the mute
     */
    private Member mutingUser;
    /**
     * The persisted mute object from the database containing the information about the mute
     */
    private Long muteId;
    private Instant muteDate;
    private Instant muteTargetDate;
    private String reason;
    private ServerChannelMessage context;
    private MessageChannel contextChannel;
    private Message message;

    /**
     * The {@link Duration} of the mute between the mute was cast and and the date it should end
     * @return The {@link Duration} between start and target date
     */
    public Duration getMuteDuration() {
        return Duration.between(muteDate, muteTargetDate);
    }
}
