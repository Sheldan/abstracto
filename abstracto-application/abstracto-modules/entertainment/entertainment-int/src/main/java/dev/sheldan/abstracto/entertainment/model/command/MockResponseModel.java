package dev.sheldan.abstracto.entertainment.model.command;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class MockResponseModel extends SlimUserInitiatedServerContext {
    private String originalText;
    private String mockingText;
}
