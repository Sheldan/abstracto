package dev.sheldan.abstracto.webservices.youtube.model.command;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import dev.sheldan.abstracto.webservices.youtube.model.YoutubeVideo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Setter
public class YoutubeVideoSearchCommandModel extends SlimUserInitiatedServerContext {
    private YoutubeVideo video;
}
