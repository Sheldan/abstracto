package dev.sheldan.abstracto.core.templating.model;

import dev.sheldan.abstracto.core.models.database.ComponentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.components.actionrow.ActionRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A full message which is ready to be send. This message can contain an arbitrary amount of embeds and a string message.
 */
@Getter
@Setter
@Builder
public class MessageToSend {
    /**
     * The collections of embeds to send. The first embed is in the same message as the string message.
     */
    @Builder.Default
    private List<MessageEmbed> embeds = new ArrayList<>();

    @Builder.Default
    private List<MessageTopLevelComponent> components = new ArrayList<>();

    @Builder.Default
    private Boolean useComponentsV2 = false;
    /**
     * The string content to be used in the first message.
     */
    @Builder.Default
    private List<String> messages = new ArrayList<>();
    /**
     * The file handle to send attached to the message.
     */
    @Builder.Default
    private List<AttachedFile> attachedFiles = new ArrayList<>();
    private MessageConfig messageConfig;
    private Long referencedMessageId;
    @Builder.Default
    private List<ActionRow> actionRows = new ArrayList<>();
    @Builder.Default
    private Map<String, ComponentConfig> componentPayloads = new HashMap<>();

    @Builder.Default
    private Boolean ephemeral = false;

    public boolean hasFilesToSend() {
        return !attachedFiles.isEmpty();
    }

    @Getter
    @Setter
    @Builder
    public static class ComponentConfig {
        private String payload;
        private String componentOrigin;
        private ComponentType componentType;
        private Class payloadType;
        private Boolean persistCallback;
    }
}
