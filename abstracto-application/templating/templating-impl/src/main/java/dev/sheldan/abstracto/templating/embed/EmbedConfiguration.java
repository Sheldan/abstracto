package dev.sheldan.abstracto.templating.embed;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class EmbedConfiguration {
    private EmbedAuthor author;
    private EmbedTitle title;
    private EmbedColor color;
    private String description;
    private String thumbnail;
    private List<EmbedField> fields;
    private EmbedFooter footer;
}
