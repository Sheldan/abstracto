package dev.sheldan.abstracto.scheduling.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto.scheduling")
public class JobConfigLoader {
    private HashMap<String, SchedulerJobProperties> jobs = new HashMap<>();
}
