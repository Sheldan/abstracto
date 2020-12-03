package dev.sheldan.abstracto.utility.models;


import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class RepostLeaderboardEntryModel {
    private Member member;
    private AUserInAServer user;
    private Integer count;
    private Integer rank;
}
