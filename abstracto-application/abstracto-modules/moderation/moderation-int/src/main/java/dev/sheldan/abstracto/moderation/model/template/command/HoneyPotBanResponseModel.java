package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HoneyPotBanResponseModel {
    private Integer bannedMemberCount;
}
