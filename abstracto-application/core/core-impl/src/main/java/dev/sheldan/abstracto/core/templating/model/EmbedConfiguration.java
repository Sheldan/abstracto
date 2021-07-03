package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The whole container object used to deserialize the whole embed configuration
 * https://raw.githubusercontent.com/DV8FromTheWorld/JDA/assets/assets/docs/embeds/07-addField.png
 */
@Getter
@Setter
@Builder
public class EmbedConfiguration {
    /**
     * The {@link EmbedAuthor} object holding the configuration for the author of the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private EmbedAuthor author;
    /**
     * The {@link EmbedTitle} object holding the configuration for the title in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private EmbedTitle title;
    /**
     * The {@link EmbedColor} object holding the configuration for the color in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private EmbedColor color;
    /**
     * The description which is going to be used in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private String description;
    /**
     * The link to the image used as a thumbnail in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private String thumbnail;
    /**
     * The link to the image used as the image in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private String imageUrl;
    /**
     * The collection containing all the objects containing the configuration for the fields in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private List<EmbedField> fields;
    /**
     * The {@link EmbedFooter} object holding the configuration for the footer in {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private EmbedFooter footer;
    /**
     * The {@link OffsetDateTime} object used as the time stamp in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private OffsetDateTime timeStamp;
    private Long referencedMessageId;
    /**
     * The message which is posted along the {@link net.dv8tion.jda.api.entities.MessageEmbed} as a normal message.
     */
    private String additionalMessage;
    private MetaEmbedConfiguration metaConfig;
    private List<ButtonConfig> buttons;
}
