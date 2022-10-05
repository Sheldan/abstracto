package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class CachedAttachment implements Serializable {
    private String url;
    private Long id;
    private String proxyUrl;
    private String fileName;
    private Boolean spoiler;
    private Integer size;
    private Integer height;
    private Integer width;
}
