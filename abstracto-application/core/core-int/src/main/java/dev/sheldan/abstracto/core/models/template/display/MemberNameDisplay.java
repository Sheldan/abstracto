package dev.sheldan.abstracto.core.models.template.display;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@Builder
public class MemberNameDisplay {
    private String userName;
    private String displayName;
    private String nickname;
    private String discriminator;
    private String userAvatarUrl;
    private String memberAvatarUrl;
    private String memberMention;

    public static MemberNameDisplay fromMember(Member member) {
        User user = member.getUser();
        String userAvatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : user.getDefaultAvatar().getUrl(4096);
        return MemberNameDisplay
                .builder()
                .memberAvatarUrl(member.getEffectiveAvatar().getUrl(4096))
                .nickname(member.getNickname())
                .userName(user.getName())
                .displayName(user.getGlobalName())
                .memberMention(member.getAsMention())
                .userAvatarUrl(userAvatar)
                .discriminator(user.getDiscriminator())
                .build();
    }
}
