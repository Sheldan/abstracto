package dev.sheldan.abstracto.moderation.model.listener;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HoneyPotReasonModel {
    private MemberDisplay memberDisplay;
    private RoleDisplay roleDisplay;
}
