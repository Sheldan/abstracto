package dev.sheldan.abstracto.customcommand.model.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomCommandResponseModel {
    private String additionalText;
}
