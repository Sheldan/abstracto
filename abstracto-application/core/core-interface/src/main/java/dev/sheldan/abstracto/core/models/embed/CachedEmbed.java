package dev.sheldan.abstracto.core.models.embed;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CachedEmbed {
    private EmbedAuthor author;
    private EmbedTitle title;
    private EmbedColor color;
    private String description;
    private String thumbnail;
    private String imageUrl;
    private List<EmbedField> fields;
    private EmbedFooter footer;
    private OffsetDateTime timeStamp;
}
