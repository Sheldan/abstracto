package dev.sheldan.abstracto.utility.models.template;

import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import dev.sheldan.abstracto.utility.models.Suggestion;
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
    private String text;
    private String reason;
    private Long originalMessageId;
    private Long originalChannelId;
    private String originalMessageUrl;
}
