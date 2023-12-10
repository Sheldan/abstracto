package dev.sheldan.abstracto.giveaway.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class GiveawayMessageModel {
    private String title;
    private String description;
    private Integer winnerCount;
    private Long giveawayId;
    @Builder.Default
    private Boolean ended = false;
    @Builder.Default
    private Boolean cancelled = false;
    @Builder.Default
    private Long joinedUserCount = 0L;
    private MemberDisplay creator;
    private MemberDisplay benefactor;
    private Instant targetDate;
    private String joinComponentId;
    @Builder.Default
    private List<MemberDisplay> winners = new ArrayList<>();

    public static GiveawayMessageModel fromGiveaway(Giveaway giveaway) {
        return GiveawayMessageModel
                .builder()
                .title(giveaway.getTitle())
                .description(giveaway.getDescription())
                .benefactor(giveaway.getBenefactor() != null ? MemberDisplay.fromAUserInAServer(giveaway.getBenefactor()) : null)
                .creator(MemberDisplay.fromAUserInAServer(giveaway.getCreator()))
                .winnerCount(giveaway.getWinnerCount())
                .joinedUserCount((long) giveaway.getParticipants().size())
                .joinComponentId(giveaway.getComponentId())
                .giveawayId(giveaway.getGiveawayId().getId())
                .targetDate(giveaway.getTargetDate())
                .build();
    }
}
