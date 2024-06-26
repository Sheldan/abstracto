package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class MemberTimeoutUpdatedModel implements FeatureAwareListenerModel {
    private ServerUser mutedUser;
    private ServerUser mutingUser;
    private Guild guild;
    private String reason;
    private Long responsibleUserId;
    private OffsetDateTime oldTimeout;
    private OffsetDateTime newTimeout;
    private Member mutedMember;
    private Member mutingMember;
    @Override
    public Long getServerId() {
        return guild.getIdLong();
    }
}
