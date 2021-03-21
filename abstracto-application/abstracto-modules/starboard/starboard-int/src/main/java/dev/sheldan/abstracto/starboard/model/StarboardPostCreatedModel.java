package dev.sheldan.abstracto.starboard.model;

import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class StarboardPostCreatedModel implements FeatureAwareListenerModel {
    private ServerChannelMessage starredMessage;
    private ServerChannelMessage starboardMessage;
    private ServerUser starredUser;
    private ServerUser lastStarrer;
    private Long starboardPostId;
    private List<Long> allStarrer;

    @Override
    public Long getServerId() {
        return starredUser.getServerId();
    }
}
