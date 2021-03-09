package dev.sheldan.abstracto.utility.models.template.commands.starboard;

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
