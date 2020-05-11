package dev.sheldan.abstracto.templating.model;

import lombok.Getter;
import lombok.Setter;

/**
 * The container class used to deserialize the embed configuration for a field in {@link net.dv8tion.jda.api.entities.MessageEmbed}
 */
@Getter
@Setter
public class EmbedField {
    /**
     * The name of the field to be set, must not be null or empty
     */
    private String name;
    /**
     * The value of the field to be set, must not be null or empty
     */
    private String value;
    /**
     * Whether or not the field should be rendered inline within the {@link net.dv8tion.jda.api.entities.MessageEmbed}.
     * This means, if multiple fields can be put on the same height in the {@link net.dv8tion.jda.api.entities.MessageEmbed} this will be done by discord.
     */
    private Boolean inline;
}
