package dev.sheldan.abstracto.core.models.command;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class ListChannelGroupsModel extends UserInitiatedServerContext {
    private List<ChannelGroupModel> groups;
}
