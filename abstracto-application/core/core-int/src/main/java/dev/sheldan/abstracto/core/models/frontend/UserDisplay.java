package dev.sheldan.abstracto.core.models.frontend;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Builder
public class UserDisplay {
    private String avatarUrl;
    private String name;
    private Long id;

    public static UserDisplay fromMember(Member member) {
        return builder()
                .avatarUrl(member.getEffectiveAvatarUrl())
                .name(member.getEffectiveName())
                .id(member.getIdLong())
                .build();
    }
}
