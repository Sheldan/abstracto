package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ImageConfig {
    private String url;
    private String description;
    @Builder.Default
    private Boolean spoiler = false;
}
