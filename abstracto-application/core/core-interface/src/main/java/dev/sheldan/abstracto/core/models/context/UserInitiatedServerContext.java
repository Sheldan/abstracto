package dev.sheldan.abstracto.core.models.context;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

@Getter @NoArgsConstructor
@Setter
@SuperBuilder
public class UserInitiatedServerContext extends ServerContext {
    private ChannelDto channel;
    private MessageChannel messageChannel;
    private Member member;
    private UserDto user;
    private UserInServerDto aUserInAServer;

}
