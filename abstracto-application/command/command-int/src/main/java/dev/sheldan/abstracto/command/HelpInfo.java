package dev.sheldan.abstracto.command;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class HelpInfo {
    private String usage;
    private String longHelp;
    private boolean templated;
}
