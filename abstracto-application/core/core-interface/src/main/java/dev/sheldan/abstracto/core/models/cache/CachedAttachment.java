package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CachedAttachment {
    private String url;
    private String proxyUrl;
    private String fileName;
    private Integer size;
    private Integer height;
    private Integer width;
}
