package dev.sheldan.abstracto.templating.model;

import lombok.Getter;
import lombok.Setter;

/**
 * The container object used to deserialize the embed configuration for the footer in the {@link net.dv8tion.jda.api.entities.MessageEmbed}
 */
@Getter
@Setter
public class EmbedFooter {
    /**
     * The text which is going to be used as the footer text
     */
    private String text;
    /**
     * The link to the image which is going to be used as the icon of the footer
     */
    private String icon;
}
