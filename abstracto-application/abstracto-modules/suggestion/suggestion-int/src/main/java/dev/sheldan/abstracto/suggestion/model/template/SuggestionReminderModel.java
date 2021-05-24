package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class SuggestionReminderModel {
    private Long serverId;
    private Long suggestionId;
    private Instant suggestionCreationDate;
    private ServerChannelMessage suggestionMessage;
    private ServerChannelMessage suggestionCommandMessage;
    private ServerUser serverUser;
}
