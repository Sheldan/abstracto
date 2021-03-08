package dev.sheldan.abstracto.core.models.cache;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CachedEmbedAuthor {
    private String name;
    private String url;
    private String avatar;
}
