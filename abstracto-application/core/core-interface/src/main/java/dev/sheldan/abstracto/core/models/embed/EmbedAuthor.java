package dev.sheldan.abstracto.core.models.embed;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmbedAuthor {
    private String name;
    private String url;
    private String avatar;
}
