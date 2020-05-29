package dev.sheldan.abstracto.templating.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The container class used to deserialize the embed configuration for the author in {@link net.dv8tion.jda.api.entities.MessageEmbed}
 */
@Getter
@Setter
@Builder
public class EmbedAuthor {
    /**
     * The name used in the {@link net.dv8tion.jda.api.entities.MessageEmbed} author
     */
    private String name;
    /**
     * The URL used in the {@link net.dv8tion.jda.api.entities.MessageEmbed} author
     */
    private String url;
    /**
     * The picture used as the avatar of the author in {@link net.dv8tion.jda.api.entities.MessageEmbed}
     */
    private String avatar;
}
