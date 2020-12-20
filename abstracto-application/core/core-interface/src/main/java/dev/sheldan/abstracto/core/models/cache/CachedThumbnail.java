package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CachedThumbnail {
    protected String url;
    protected String proxyUrl;
    protected Integer width;
    protected Integer height;

}
