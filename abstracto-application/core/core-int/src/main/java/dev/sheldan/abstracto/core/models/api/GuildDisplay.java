package dev.sheldan.abstracto.core.models.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuildDisplay {
    private Long id;
    private String name;
    private String iconUrl;
    private String bannerUrl;
}
