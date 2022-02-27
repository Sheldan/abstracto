package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ServerChoices {
    private Map<String, ServerChoice> commonGuilds;
    private Long userId;
    private Long messageId;
}
