package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@Builder
public class MemberKickedModel implements FeatureAwareListenerModel {
    private ServerUser kickedServerUser;
    private ServerUser kickingServerUser;
    private Guild guild;
    private String reason;
    private Long responsibleUserId;
    private User kickedUser;
    private User kickingUser;
    @Override
    public Long getServerId() {
        return guild.getIdLong();
    }
}
