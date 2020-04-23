package dev.sheldan.abstracto.experience.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto.experience")
public class ExperienceConfig {
    /**
     * The default min experience range from the properties file. This is used, when the bot joins a new guild.
     */
    private Integer minExp;

    /**
     * The default max experience range from the properties file. This is used, when the bot joins a new guild.
     */
    private Integer maxExp;

    /**
     * The default multiplier from the properties file. This is used, when the bot joins a new guild.
     */
    private Integer expMultiplier;

    /**
     * The defaul maxLevel from the properties file. This configuration applies globally, as the amount of levels does not depend on the server.
     */
    private Integer maxLvl;
}