package dev.sheldan.abstracto.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

@Getter
@Setter
@Builder
public class MessageToSend {
    private List<MessageEmbed> embeds;
    private String message;
}
