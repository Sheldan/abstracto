package dev.sheldan.abstracto.entertainment.model.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class CreditsLeaderboardEntry {
    private MemberDisplay memberDisplay;
    private Member member;
    private Long credits;
    private Integer rank;
}
