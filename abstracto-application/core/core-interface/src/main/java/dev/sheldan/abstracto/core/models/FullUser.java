package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

@Setter
@Getter
@Builder
public class FullUser {
    private AUserInAServer aUserInAServer;
    private Member member;
}
