package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.time.Duration;


@Getter
@SuperBuilder
@Setter
public class MuteLog extends UserInitiatedServerContext {

    private Member mutedUser;
    private Member mutingUser;
    private Message message;
    private Mute mute;

    public Duration getMuteDuration() {
        return Duration.between(mute.getMuteDate(), mute.getMuteTargetDate());
    }
}
