package dev.sheldan.abstracto.core.command.models.condition;

import dev.sheldan.abstracto.core.command.condition.ConditionDetail;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GenericConditionModel {
    private GuildChannelMember guildChannelMember;
    private ConditionDetail conditionDetail;

}
