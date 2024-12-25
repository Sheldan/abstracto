package dev.sheldan.abstracto.giveaway.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GiveawayKeyDisplayModel {
    private String key;
    private String name;
    private Long id;
    private String description;
    private Boolean used;
    private MemberDisplay creator;
    private MemberDisplay benefactor;
    private MemberDisplay winner;
}
