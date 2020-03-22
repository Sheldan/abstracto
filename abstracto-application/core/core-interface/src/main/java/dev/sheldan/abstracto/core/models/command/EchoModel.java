package dev.sheldan.abstracto.core.models.command;


import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
public class EchoModel extends UserInitiatedServerContext {
    private String text;
}
