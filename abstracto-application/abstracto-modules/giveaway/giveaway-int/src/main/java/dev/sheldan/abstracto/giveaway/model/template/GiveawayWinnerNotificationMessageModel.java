package dev.sheldan.abstracto.giveaway.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class GiveawayWinnerNotificationMessageModel {
    private String title;
    private String description;
    private String key;
    private Long giveawayId;
    private MemberDisplay creator;
    private MemberDisplay benefactor;

    public static GiveawayWinnerNotificationMessageModel fromGiveaway(Giveaway giveaway, String key) {
        return GiveawayWinnerNotificationMessageModel
                .builder()
                .title(giveaway.getTitle())
                .description(giveaway.getDescription())
                .key(key)
                .benefactor(giveaway.getBenefactor() != null ? MemberDisplay.fromAUserInAServer(giveaway.getBenefactor()) : null)
                .creator(MemberDisplay.fromAUserInAServer(giveaway.getCreator()))
                .giveawayId(giveaway.getGiveawayId().getId())
                .build();
    }
}
