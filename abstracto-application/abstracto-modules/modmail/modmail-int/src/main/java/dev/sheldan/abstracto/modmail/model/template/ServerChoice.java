package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServerChoice {
    private String serverName;
    private Long serverId;
    @Builder.Default
    private Boolean appealModmail = false;
}
