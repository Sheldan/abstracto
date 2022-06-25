package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;


/**
 * Used when rendering the notification when a member was muted. The template is: "mute_log_embed"
 */
@Getter
@SuperBuilder
@Setter
public class MuteContext {
    private Member mutedUser;
    private Member mutingUser;
    private Long muteId;
    private Instant muteTargetDate;
    private String reason;
    private Long channelId;

}
