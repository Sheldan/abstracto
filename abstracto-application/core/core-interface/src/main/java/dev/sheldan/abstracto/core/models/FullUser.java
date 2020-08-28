package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

@Setter
@Getter
@Builder
public class FullUser {
    private AUser auser;
    private User user;
}
