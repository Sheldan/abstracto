package dev.sheldan.abstracto.core.commands.utility.model;


import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
public class EchoModel extends UserInitiatedServerContext {
    private String text;
}
