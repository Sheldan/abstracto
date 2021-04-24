package dev.sheldan.abstracto.core.models.cache;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class CachedEmbedAuthor implements Serializable {
    private String name;
    private String url;
    private String avatar;
}
