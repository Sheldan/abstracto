package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class MemberLeaveModel implements FeatureAwareListenerModel {
    private ServerUser leavingUser;
    private Member member;
    @Override
    public Long getServerId() {
        return leavingUser.getServerId();
    }
}
