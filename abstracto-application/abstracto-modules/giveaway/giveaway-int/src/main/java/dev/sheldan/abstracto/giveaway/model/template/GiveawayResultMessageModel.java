package dev.sheldan.abstracto.giveaway.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GiveawayResultMessageModel {
    private String title;
    private Long messageId;
    private List<MemberDisplay> winners;
}
