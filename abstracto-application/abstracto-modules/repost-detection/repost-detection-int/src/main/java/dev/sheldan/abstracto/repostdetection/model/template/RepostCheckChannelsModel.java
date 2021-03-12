package dev.sheldan.abstracto.repostdetection.model.template;

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
