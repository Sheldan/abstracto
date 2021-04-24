package dev.sheldan.abstracto.core.models.cache;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class CachedEmbedTitle implements Serializable {
    private String title;
    private String url;
}
