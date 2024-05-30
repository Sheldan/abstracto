package dev.sheldan.abstracto.core.models.template.display;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.utils.MemberUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@Builder
public class UserDisplay {
    private Long id;
    private String userMention;
    private String discriminator;
    private String name;
    private String avatarUrl;

    public static UserDisplay fromUser(User user) {
        return UserDisplay
                .builder()
                .userMention(MemberUtils.getUserAsMention(user.getIdLong()))
                .name(user.getEffectiveName())
                .discriminator(user.getDiscriminator())
                .id(user.getIdLong())
                .avatarUrl(user.getEffectiveAvatarUrl())
                .build();
    }

    public static UserDisplay fromServerUser(ServerUser serverUser) {
        return UserDisplay
                .builder()
                .userMention(MemberUtils.getUserAsMention(serverUser.getUserId()))
                .id(serverUser.getUserId())
                .build();
    }

    public static UserDisplay fromId(Long id) {
        return UserDisplay
                .builder()
                .userMention(MemberUtils.getUserAsMention(id))
                .id(id)
                .build();
    }

}
