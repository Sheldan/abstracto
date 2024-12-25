package dev.sheldan.abstracto.giveaway.model.template;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GiveawayKeysDisplayModel {
    private List<GiveawayKeyDisplayModel> keys;
}
