package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CachedEmote implements Serializable {
    private String emoteName;
    private Long emoteId;
    private Boolean external;
    private String imageURL;
    private Boolean custom;
    private Boolean animated;
    private Long serverId;
}
