package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostTargetModelEntry {
    private String name;
    private ChannelDisplay channel;
    private PostTargetChannelGroup channelGroup;
    private Boolean disabled;

    @Getter
    @Builder
    public static class PostTargetChannelGroup {
        private String name;
        private Boolean disabled;
        private List<ChannelDisplay> additionalChannels;
    }
}

