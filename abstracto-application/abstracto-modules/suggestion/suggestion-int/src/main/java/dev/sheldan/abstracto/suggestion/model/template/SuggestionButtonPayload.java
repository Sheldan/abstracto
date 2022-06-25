package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionDecision;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SuggestionButtonPayload implements ButtonPayload {
    private Long suggestionId;
    private Long serverId;
    private SuggestionDecision decision;
}
