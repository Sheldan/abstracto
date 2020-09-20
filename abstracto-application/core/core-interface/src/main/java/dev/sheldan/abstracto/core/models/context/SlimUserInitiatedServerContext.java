package dev.sheldan.abstracto.core.models.context;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

@Getter
@NoArgsConstructor
@Setter
@SuperBuilder
public class SlimUserInitiatedServerContext extends SlimServerContext {
    private MessageChannel channel;
    private Member member;
    private Message message;
}
