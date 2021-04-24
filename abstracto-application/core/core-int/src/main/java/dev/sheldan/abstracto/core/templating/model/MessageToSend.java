package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A full message which is ready to be send. This message can contain an arbitrary amount of embeds and a string message.
 */
@Getter
@Setter
@Builder
public class MessageToSend {
    /**
     * The collections of embeds to be send. The first embed is in the same message as the string message.
     */
    @Builder.Default
    private List<MessageEmbed> embeds = new ArrayList<>();
    /**
     * The string content to be used in the first message.
     */
    private List<String> messages;
    /**
     * The file handle to send attached to the message.
     */
    private File fileToSend;
    private MessageConfig messageConfig;

    public boolean hasFileToSend() {
        return fileToSend != null;
    }
}
