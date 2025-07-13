package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SectionThumbnail implements SectionAccessoryConfig {

    private String url;
    @Builder.Default
    private Boolean spoiler = false;
    private String description;
    private Integer uniqueId;

    @Override
    public SectionAccessoryType getType() {
        return SectionAccessoryType.THUMBNAIL;
    }
}
