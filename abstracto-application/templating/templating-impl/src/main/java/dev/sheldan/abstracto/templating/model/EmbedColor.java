package dev.sheldan.abstracto.templating.model;

import lombok.Getter;
import lombok.Setter;

/**
 * The container class used to deserialize the embed configuration for the color in {@link net.dv8tion.jda.api.entities.MessageEmbed}
 */
@Setter
@Getter
public class EmbedColor {
    /**
     * The red part of RGB
     */
    private Integer r;
    /**
     * The green part of RGB
     */
    private Integer g;
    /**
     * The blue part of RGB
     */
    private Integer b;
}
