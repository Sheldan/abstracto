package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.io.Serializable;

@Setter
@Getter
@Builder
public class FullUserInServer implements Serializable {
    private AUserInAServer aUserInAServer;
    private transient Member member;
}
