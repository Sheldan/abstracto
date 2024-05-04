package dev.sheldan.abstracto.core.models.frontend;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.utils.MemberUtils;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Getter
@Builder
public class UserDisplay {
    private String avatarUrl;
    private String name;
    private String discriminator;
    private String userMention;
    private Long id;

    public static UserDisplay fromMember(Member member) {
        return builder()
                .avatarUrl(member.getEffectiveAvatarUrl())
                .name(member.getEffectiveName())
                .userMention(MemberUtils.getUserAsMention(member.getIdLong()))
                .id(member.getIdLong())
                .build();
    }

    public static UserDisplay fromUser(User user) {
        return builder()
                .discriminator(user.getDiscriminator())
                .avatarUrl(user.getEffectiveAvatarUrl())
                .name(user.getEffectiveName())
                .userMention(MemberUtils.getUserAsMention(user.getIdLong()))
                .id(user.getIdLong())
                .build();
    }

    public static UserDisplay fromServerUser(ServerUser user) {
        return builder()
                .userMention(MemberUtils.getUserAsMention(user.getUserId()))
                .id(user.getUserId())
                .build();
    }

    public static UserDisplay fromId(Long id) {
        return builder()
                .userMention(MemberUtils.getUserAsMention(id))
                .id(id)
                .build();
    }
}
