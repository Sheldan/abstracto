package dev.sheldan.abstracto.twitch.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GoLiveNotificationModel {
    private String channelName;
    private StreamSectionDisplay currentSection;
    private List<StreamSectionDisplay> pastSections;
    private Boolean mature;
    private String streamURL;
    private String randomString;
    private String streamerAvatarURL;
}
