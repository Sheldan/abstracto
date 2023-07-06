package dev.sheldan.abstracto.twitch.model.template;

import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TwitchStreamerDisplayModel {
    private String name;
    private ChannelDisplay targetChannel;
    private Boolean deleteNotifications;
    private Boolean showNotifications;
    private String streamerURL;
}
