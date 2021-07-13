package dev.sheldan.abstracto.core.models.template.display;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class MemberDisplay {
    private String memberMention;
    private Long userId;
    private Long serverId;

    public static MemberDisplay fromMember(Member member) {
        return MemberDisplay
                .builder()
                .memberMention(member.getAsMention())
                .serverId(member.getGuild().getIdLong())
                .userId(member.getIdLong())
                .build();
    }
}
