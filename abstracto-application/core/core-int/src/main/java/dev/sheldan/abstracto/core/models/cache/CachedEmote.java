package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CachedEmote {
    private String emoteName;
    private Long emoteId;
    private Boolean external;
    private String imageURL;
    private Boolean custom;
    private Boolean animated;
    private Long serverId;
}
