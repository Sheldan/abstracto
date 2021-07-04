package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClosingProgressModel {
    private Integer loggedMessages;
    private Integer totalMessages;
}
