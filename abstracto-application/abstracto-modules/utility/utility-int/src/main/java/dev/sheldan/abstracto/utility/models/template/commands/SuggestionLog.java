package dev.sheldan.abstracto.utility.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@SuperBuilder
public class SuggestionLog extends UserInitiatedServerContext {
    private Suggestion suggestion;
    private Member suggester;
    private AUserInAServer suggesterUser;
    private String text;
    private String reason;
    private Long originalMessageId;
    private Long originalChannelId;
    private String originalMessageUrl;
}
