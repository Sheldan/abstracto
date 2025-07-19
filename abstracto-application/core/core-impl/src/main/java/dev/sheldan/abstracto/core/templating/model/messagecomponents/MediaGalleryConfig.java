package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class MediaGalleryConfig implements ComponentConfig {
    private List<ImageConfig> images;
    private Integer uniqueId;
    @Override
    public ComponentTypeConfig getType() {
        return ComponentTypeConfig.MEDIA_GALLERY;
    }
}
