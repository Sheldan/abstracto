package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.provider.ChannelGroupInformation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ChannelGroupModel {
    private String name;
    private String typeKey;
    private Boolean enabled;
    private List<ChannelGroupChannelModel> channels;
    private ChannelGroupInformation channelGroupInformation;
}
