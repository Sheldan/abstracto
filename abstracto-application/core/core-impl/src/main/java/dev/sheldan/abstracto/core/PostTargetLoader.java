package dev.sheldan.abstracto.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
@Getter
@Setter
@PropertySource("classpath:abstracto.properties")
@ConfigurationProperties(prefix = "abstracto")
public class PostTargetLoader {

    private HashMap<String, String> postTargets = new HashMap<>();

    public List<String> getPostTargetsAsList() {
        List<String> targets = new ArrayList<>();
        if(postTargets == null || postTargets.size() == 0) {
            return targets;
        }
        postTargets.values().forEach(s -> targets.addAll(Arrays.asList(s.split(","))));
        return targets;
    }
}
