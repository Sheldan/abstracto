package dev.sheldan.abstracto.starboard.model.template;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;


@Getter
@Setter
@Builder
public class StarStatsUser {
    private AUserInAServer user;
    private Member member;
    private Integer starCount;


}
