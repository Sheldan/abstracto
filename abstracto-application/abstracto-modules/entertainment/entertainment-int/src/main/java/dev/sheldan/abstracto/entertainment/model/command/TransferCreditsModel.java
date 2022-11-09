package dev.sheldan.abstracto.entertainment.model.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TransferCreditsModel {
    private Integer credits;
    private MemberDisplay sourceMember;
    private MemberDisplay targetMember;
}
