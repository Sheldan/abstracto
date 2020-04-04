package dev.sheldan.abstracto.core.models.listener;

import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@SuperBuilder
public class MessageEmbeddedModel extends UserInitiatedServerContext {
    private CachedMessage embeddedMessage;
    private Member author;
    private TextChannel sourceChannel;
    private Member embeddingUser;
}
