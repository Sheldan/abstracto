package dev.sheldan.abstracto.entertainment.model.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class PressFJoinModel {
    private MemberDisplay memberDisplay;
    private Long messageId;
}
