package dev.sheldan.abstracto.utility.models.template.commands.starboard;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class StarStatsModel {
    private List<StarStatsPost> topPosts;
    private List<StarStatsUser> starReceiver;
    private List<StarStatsUser> starGiver;
    private Integer totalStars;
    private List<String> badgeEmotes;
    private Integer starredMessages;
}
