package dev.sheldan.abstracto.core.templating.model;

import dev.sheldan.abstracto.core.templating.model.messagecomponents.ButtonConfig;
import dev.sheldan.abstracto.core.templating.model.messagecomponents.ComponentConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The whole container object used to deserialize the whole embed configuration
 * https://raw.githubusercontent.com/DV8FromTheWorld/JDA/assets/assets/docs/embeds/07-addField.png
 */
@Getter
@Setter
@Builder
public class MessageConfiguration {
    private List<EmbedConfiguration> embeds;
    private List<ComponentConfig> components;
    private Long referencedMessageId;
    /**
     * The message which is posted along the {@link net.dv8tion.jda.api.entities.MessageEmbed} as a normal message.
     */
    private String additionalMessage;
    private MetaMessageConfiguration messageConfig;
    private List<ButtonConfig> buttons;
    private List<SelectionMenuConfig> selectionMenus;
    private List<FileConfig> files;
}
