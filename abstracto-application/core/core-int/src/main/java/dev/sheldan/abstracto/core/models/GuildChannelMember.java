package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.*;

@Getter
@Setter
@Builder
public class GuildChannelMember {
    private Guild guild;
    private GuildChannel textChannel;
    private Member member;
}
