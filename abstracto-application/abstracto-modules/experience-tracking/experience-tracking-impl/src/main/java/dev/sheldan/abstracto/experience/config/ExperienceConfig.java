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
    private Integer minExp;
    private Integer maxExp;
    private Integer expMultiplier;
    private Integer maxLvl;
}