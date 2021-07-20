package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ModMailChannelNameModel {
    private Long serverId;
    private Long userId;
    private Instant currentDate;
    private String randomText;
    private String uuid;
}
