package dev.sheldan.abstracto.scheduling.config;

import dev.sheldan.abstracto.scheduling.model.SchedulerJobProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Makes the job configuration in each of the property files accessible and usable. This causes the jobs to be automatically loaded and scheduled if they appear in a property file
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto.scheduling")
public class JobConfigLoader {
    private HashMap<String, SchedulerJobProperties> jobs = new HashMap<>();
}
