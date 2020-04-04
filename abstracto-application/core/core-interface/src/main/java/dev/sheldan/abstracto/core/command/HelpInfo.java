package dev.sheldan.abstracto.core.command;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class HelpInfo {
    private String usage;
    private String longHelp;
    private boolean templated;
}
