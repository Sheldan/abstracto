package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@Builder
public class RoleAddedModel implements FeatureAwareListenerModel {
    private ServerUser targetUser;
    private Member targetMember;
    private Role role;
    private Long roleId;

    @Override
    public Long getServerId() {
        return targetUser.getServerId();
    }
}
