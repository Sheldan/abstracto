package dev.sheldan.abstracto.core.models.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CachedEmbed {
    private CachedEmbedAuthor author;
    private CachedEmbedTitle title;
    private CachedEmbedColor color;
    private String description;
    private String thumbnail;
    private String imageUrl;
    private List<CachedEmbedField> fields;
    private CachedEmbedFooter footer;
    private OffsetDateTime timeStamp;
}
