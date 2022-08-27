package dev.sheldan.abstracto.core.models.template.display;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class MemberNameDisplay {
    private String userName;
    private String nickname;
    private String discriminator;
    private String userAvatarUrl;
    private String memberAvatarUrl;

    public static MemberNameDisplay fromMember(Member member) {
        return MemberNameDisplay
                .builder()
                .memberAvatarUrl(member.getAvatarUrl())
                .nickname(member.getNickname())
                .userName(member.getUser().getName())
                .userAvatarUrl(member.getUser().getAvatarUrl())
                .discriminator(member.getUser().getDiscriminator())
                .build();
    }
}
