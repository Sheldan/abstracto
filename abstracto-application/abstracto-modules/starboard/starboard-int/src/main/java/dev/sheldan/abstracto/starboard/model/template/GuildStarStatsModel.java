package dev.sheldan.abstracto.starboard.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GuildStarStatsModel {
    private List<StarStatsPost> topPosts;
    private List<StarStatsUser> starReceiver;
    private List<StarStatsUser> starGiver;
    private Integer totalStars;
    private List<String> badgeEmotes;
    private Integer starredMessages;
}
