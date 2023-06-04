package dev.sheldan.abstracto.core.interaction.menu;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SelectMenuConfigModel {
    private String selectMenuId;
    private SelectMenuPayload selectMenuPayload;
    private Class payloadType;
    private String origin;
}
