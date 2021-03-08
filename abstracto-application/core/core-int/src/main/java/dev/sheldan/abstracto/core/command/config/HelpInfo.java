package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HelpInfo {
    private String longHelp;
    private String example;
    @Builder.Default
    private boolean hasExample = false;
    @Builder.Default
    private boolean templated = false;
}
