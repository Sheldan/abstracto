package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class BoostTimeUpdatedModel implements FeatureAwareListenerModel {
    private Member member;
    private OffsetDateTime oldTime;
    private OffsetDateTime newTime;

    @Override
    public Long getServerId() {
        return member.getGuild().getIdLong();
    }
}
