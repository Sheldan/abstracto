package dev.sheldan.abstracto.core.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "abstracto")
public class DynamicKeyLoader {

    private HashMap<String, String> postTargets = new HashMap<>();
    private HashMap<String, String> emoteNames = new HashMap<>();
    private HashMap<String, String> defaultEmotes = new HashMap<>();

    public List<String> getPostTargetsAsList() {
        return getHashMapAsList(postTargets);
    }

    public List<String> getEmoteNamesAsList() {
        return getHashMapAsList(emoteNames);
    }

    @NotNull
    private List<String> getHashMapAsList(HashMap<String, String> emoteNames) {
        List<String> emotes = new ArrayList<>();
        if (emoteNames == null || emoteNames.size() == 0) {
            return emotes;
        }
        emoteNames.values().forEach(s -> emotes.addAll(Arrays.asList(s.split(","))));
        return emotes;
    }
}
