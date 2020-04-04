package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
@Setter
@Builder
public class MessageToSend {
    private MessageEmbed embed;
    private String message;
}
