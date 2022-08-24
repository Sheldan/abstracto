package dev.sheldan.abstracto.entertainment.model.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MockResponseModel {
    private String originalText;
    private String mockingText;
}
