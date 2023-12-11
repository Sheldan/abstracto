package dev.sheldan.abstracto.entertainment.model.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class PressFResultModel {
    private Long userCount;
    private String text;
    private Long messageId;
}
