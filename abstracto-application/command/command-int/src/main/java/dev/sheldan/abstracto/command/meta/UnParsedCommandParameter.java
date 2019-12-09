package dev.sheldan.abstracto.command.meta;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class UnParsedCommandParameter {
    private List<String> parameters;
}
