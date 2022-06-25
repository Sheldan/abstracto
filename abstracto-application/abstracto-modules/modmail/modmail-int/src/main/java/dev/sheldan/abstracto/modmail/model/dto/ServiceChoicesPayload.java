package dev.sheldan.abstracto.modmail.model.dto;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import dev.sheldan.abstracto.modmail.model.template.ServerChoices;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class ServiceChoicesPayload implements ButtonPayload {
    private Map<String, ServerChoicePayload> choices;
    private Long userId;
    private Long messageId;

    public static ServiceChoicesPayload fromServerChoices(ServerChoices choices) {
        Map<String, ServerChoicePayload> newChoices = new HashMap<>();
        choices.getCommonGuilds().forEach((s, choice) -> newChoices.put(s, ServerChoicePayload.fromServerChoice(choice)));
        return ServiceChoicesPayload
                .builder()
                .userId(choices.getUserId())
                .messageId(choices.getMessageId())
                .choices(newChoices)
                .build();
    }
}
