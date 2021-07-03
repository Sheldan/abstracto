package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ButtonMetaConfig {
    private Boolean forceNewRow;
    private Boolean generateRandomUUID;
    private String buttonOrigin;
    private Boolean persistCallback;
}
