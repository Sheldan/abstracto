package dev.sheldan.abstracto.core.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmoteDto {
    private Integer Id;
    private String name;
    private String emoteKey;
    private Long emoteId;
    private Boolean animated;
    private Boolean custom;
    private ServerDto server;
}
