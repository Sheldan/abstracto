package dev.sheldan.abstracto.utility.models.template.commands.starboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@Getter
@Setter
@Builder
public class MemberStarStatsModel {
    private List<StarStatsPost> topPosts;
    private Long receivedStars;
    private Long givenStars;
    private List<String> badgeEmotes;
    private Member member;
}
