package dev.sheldan.abstracto.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The container class used to deserialize the embed configuration used in the title in {@link net.dv8tion.jda.api.entities.MessageEmbed}
 */
@Getter
@Setter
@Builder
public class EmbedTitle {
    /**
     * The text which is going to be used as the title of the embed
     */
    private String title;
    /**
     * The link which is used when clicking on the title of the embed
     */
    private String url;
}
