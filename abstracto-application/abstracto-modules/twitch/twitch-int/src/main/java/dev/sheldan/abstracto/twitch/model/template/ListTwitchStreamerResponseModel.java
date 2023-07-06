package dev.sheldan.abstracto.twitch.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ListTwitchStreamerResponseModel {
    private List<TwitchStreamerDisplayModel> streamers;
}
