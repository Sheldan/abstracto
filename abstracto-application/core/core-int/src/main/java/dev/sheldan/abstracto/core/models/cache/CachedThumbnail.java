package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class CachedThumbnail implements Serializable {
    protected String url;
    protected String proxyUrl;
    protected Integer width;
    protected Integer height;

}
