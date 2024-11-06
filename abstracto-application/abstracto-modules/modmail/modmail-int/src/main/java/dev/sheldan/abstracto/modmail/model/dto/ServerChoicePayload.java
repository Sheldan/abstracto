package dev.sheldan.abstracto.modmail.model.dto;

import dev.sheldan.abstracto.modmail.model.template.ServerChoice;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServerChoicePayload {
    private Long serverId;
    @Builder.Default
    private Boolean appealModmail = false;

    public static ServerChoicePayload fromServerChoice(ServerChoice choice) {
        return ServerChoicePayload
                .builder()
                .serverId(choice.getServerId())
                .appealModmail(choice.getAppealModmail())
                .build();
    }
}
