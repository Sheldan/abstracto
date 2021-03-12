package dev.sheldan.abstracto.repostdetection.model.template;

import dev.sheldan.abstracto.core.models.FullChannel;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RepostCheckChannelGroupDisplayModel {
    private RepostCheckChannelGroup channelGroup;
    private List<FullChannel> channels;
}
