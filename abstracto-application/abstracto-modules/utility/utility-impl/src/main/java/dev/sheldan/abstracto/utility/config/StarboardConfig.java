package dev.sheldan.abstracto.utility.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto.starboard")
public class StarboardConfig {
   private List<Integer> lvl = new ArrayList<>();
   private List<String> badge = new ArrayList<>();
}
