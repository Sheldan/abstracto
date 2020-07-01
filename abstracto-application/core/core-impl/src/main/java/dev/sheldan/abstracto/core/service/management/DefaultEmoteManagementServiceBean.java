package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.DefaultEmote;
import dev.sheldan.abstracto.core.repository.DefaultEmoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultEmoteManagementServiceBean implements DefaultEmoteManagementService {

    @Autowired
    private DefaultEmoteRepository repository;

    @Override
    public DefaultEmote getDefaultEmote(String key) {
        return repository.getByEmoteKey(key);
    }

    @Override
    public List<DefaultEmote> getAllDefaultEmotes() {
        return repository.findAll();
    }

    @Override
    public List<String> getDefaultEmoteNames() {
        return getAllDefaultEmotes().stream().map(DefaultEmote::getEmoteKey).collect(Collectors.toList());
    }
}
