package dev.sheldan.abstracto.twitch.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GoOfflineNotificationModel {
    private String channelName;
    private List<StreamSectionDisplay> pastSections;
    private String offlineImageURL;
    private String avatarURL;
}
