package dev.sheldan.abstracto.webservices.youtube.model.command;

import dev.sheldan.abstracto.webservices.youtube.model.YoutubeVideo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class YoutubeVideoSearchCommandModel {
    private YoutubeVideo video;
}
