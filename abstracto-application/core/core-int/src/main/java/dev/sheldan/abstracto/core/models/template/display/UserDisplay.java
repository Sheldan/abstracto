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
    private Long userId;
    private String userMention;

    public static UserDisplay fromUser(User user) {
        return UserDisplay
                .builder()
                .userMention(MemberUtils.getUserAsMention(user.getIdLong()))
                .userId(user.getIdLong())
                .build();
    }

    public static UserDisplay fromServerUser(ServerUser serverUser) {
        return UserDisplay
                .builder()
                .userMention(MemberUtils.getUserAsMention(serverUser.getUserId()))
                .userId(serverUser.getUserId())
                .build();
    }

    public static UserDisplay fromId(Long id) {
        return UserDisplay
                .builder()
                .userMention(MemberUtils.getUserAsMention(id))
                .userId(id)
                .build();
    }

}
