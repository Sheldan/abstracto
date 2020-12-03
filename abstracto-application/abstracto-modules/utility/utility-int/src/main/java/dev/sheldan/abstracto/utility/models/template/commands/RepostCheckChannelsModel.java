package dev.sheldan.abstracto.utility.models.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RepostCheckChannelsModel {
    private List<RepostCheckChannelGroupDisplayModel> repostCheckChannelGroups;
}
