package dev.sheldan.abstracto.moderation.model.template.listener;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@Builder
public class UserUnBannedListenerModel {
    private User unBannedUser;
    private User unBanningUser;
}
