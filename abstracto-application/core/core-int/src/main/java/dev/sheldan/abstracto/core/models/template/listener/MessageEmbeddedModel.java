package dev.sheldan.abstracto.core.models.template.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@SuperBuilder
public class MessageEmbeddedModel extends UserInitiatedServerContext {
    private CachedMessage embeddedMessage;
    private User author;
    private TextChannel sourceChannel;
    private Member embeddingUser;
}
