package dev.sheldan.abstracto.core.models.context;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

@Getter @NoArgsConstructor
@Setter
@SuperBuilder
public class UserInitiatedServerContext extends ServerContext {
    private AChannel channel;
    private MessageChannel messageChannel;
    private Member member;
    private AUser user;
    private Message message;
    private AUserInAServer aUserInAServer;

}
