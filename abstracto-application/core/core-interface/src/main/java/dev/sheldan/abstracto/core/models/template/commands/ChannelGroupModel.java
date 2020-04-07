package dev.sheldan.abstracto.core.models.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ChannelGroupModel {
    private String name;
    private List<ChannelGroupChannelModel> channels;
}
