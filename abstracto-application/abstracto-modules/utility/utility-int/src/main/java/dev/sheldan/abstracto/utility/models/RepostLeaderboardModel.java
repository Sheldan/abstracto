package dev.sheldan.abstracto.utility.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@Getter
@Setter
@Builder
public class RepostLeaderboardModel {
    private List<RepostLeaderboardEntryModel> entries;
    private Guild guild;
    private RepostLeaderboardEntryModel userExecuting;
    private Member member;
}
