package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class TopLevelMediaGalleryConfig extends MediaGalleryConfig implements ComponentConfig {
    @Override
    public ComponentTypeConfig getType() {
        return ComponentTypeConfig.MEDIA_GALLERY;
    }
}
