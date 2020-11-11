package dev.sheldan.abstracto.statistic.emotes.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PersistingEmote {
    private Long emoteId;
    private String emoteName;
    private Boolean animated;
    private Boolean external;
    private String externalUrl;
    private Long count;
    private Long serverId;
}
