package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class MemberTimeoutUpdatedModel implements FeatureAwareListenerModel {
    private ServerUser timeoutUser;
    private User user;
    private Guild guild;
    private OffsetDateTime oldTimeout;
    private OffsetDateTime newTimeout;
    private Member member;
    private GuildMemberUpdateTimeOutEvent event;
    @Override
    public Long getServerId() {
        return guild.getIdLong();
    }
}
