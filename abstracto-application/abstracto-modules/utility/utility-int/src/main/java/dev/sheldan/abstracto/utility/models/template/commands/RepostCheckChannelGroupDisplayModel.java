package dev.sheldan.abstracto.utility.models.template.commands;

import dev.sheldan.abstracto.core.models.FullChannel;
import dev.sheldan.abstracto.utility.models.database.RepostCheckChannelGroup;
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
